package com.caco3.mvk.myaudios;

import com.caco3.mvk.audiodownload.AudioDownloader;
import com.caco3.mvk.data.appuser.AppUser;
import com.caco3.mvk.data.audio.AudiosRepository;
import com.caco3.mvk.search.DataSetFilter;
import com.caco3.mvk.util.Integers;
import com.caco3.mvk.vk.Vk;
import com.caco3.mvk.vk.VkException;
import com.caco3.mvk.vk.audio.Audio;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.exceptions.Exceptions;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import timber.log.Timber;


/*package*/ class MyAudiosPresenterImpl implements MyAudiosPresenter {
  static final Comparator<Audio> audioByPositionComparator = new Comparator<Audio>() {
    @Override
    public int compare(Audio o1, Audio o2) {
      return Integers.compare(o1.getVkPlaylistPosition(), o2.getVkPlaylistPosition());
    }
  };
  final Mode selectMode = new SelectMode();
  final Mode normalMode = new NormalMode();

  private MyAudiosView view = NullAudiosView.INSTANCE;
  private AppUser currentAppUser;
  private AudiosRepository audiosRepository;
  private Vk vk;
  private Subscriber<List<Audio>> vkAudiosSubscriber = null;
  private AudioDownloader audioDownloader;
  private DataSetFilter<Audio> audiosFilter = new AudiosFilter();
  private List<Audio> cachedAudios;
  private String searchQuery = "";
  final List<Audio> selectedInActionMode = new ArrayList<>();
  Mode mode = normalMode;

  @Inject
  /*package*/ MyAudiosPresenterImpl(AppUser appUser, AudiosRepository audiosRepository,
                                    Vk vk, AudioDownloader audioDownloader) {
    this.currentAppUser = appUser;
    this.audiosRepository = audiosRepository;
    this.vk = vk;
    this.audioDownloader = audioDownloader;
  }

  @Override
  public void onViewAttached(MyAudiosView view) {
    this.view = view;
    initView();
  }

  private void initView() {
    view.showGlobalProgress();
    if (areAudiosLoadingFromVk()) {
      view.showRefreshLayout();
    }
    showAudios();
    if (mode == selectMode) {
      view.startSelectMode();
    }
  }

  private boolean areAudiosLoadingFromVk() {
    return vkAudiosSubscriber != null && !vkAudiosSubscriber.isUnsubscribed();
  }

  private void showAudios() {
    if (areAudiosCached()) {
      if (isSearching()) {
        view.showAudios(audiosFilter.filter(cachedAudios, searchQuery));
      } else {
        view.showAudios(cachedAudios);
      }
      view.hideGlobalProgress();
      for(Audio selected : selectedInActionMode) {
        view.showAudioSelected(selected);
      }
    } else {
      loadAudiosFromRepository();
    }
  }

  private boolean areAudiosCached() {
    return cachedAudios != null;
  }

  private boolean isSearching() {
    return searchQuery != null && !searchQuery.isEmpty();
  }

  private void loadAudiosFromRepository() {
    Observable.fromCallable(new Callable<List<Audio>>() {
      @Override
      public List<Audio> call() {
        List<Audio> audios = audiosRepository
                .getAllByVkUserId(currentAppUser.getUserToken().getVkUserId());
        Collections.sort(audios, audioByPositionComparator);
        return audios;
      }
    }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Action1<List<Audio>>() {
              @Override
              public void call(List<Audio> audios) {
                setCache(audios);
                showAudios();
                view.hideGlobalProgress();
              }
            });
  }

  private void setCache(List<Audio> audios) {
    cachedAudios = audios;
  }

  @Override
  public void onViewDetached(MyAudiosView view) {
    this.view = NullAudiosView.INSTANCE;
  }

  @Override
  public void onRefreshRequest() {
    loadAudiosFromVk();
  }

  private void loadAudiosFromVk() {
    view.showRefreshLayout();
    vkAudiosSubscriber = new VkAudiosSubscriber();
    Observable.fromCallable(new Callable<List<Audio>>() {
      @Override
      public List<Audio> call() throws Exception {
        List<Audio> audios = vk.audios().get();
        for(int i = 0, length = audios.size(); i < length; i++) {
          audios.get(i).setVkPlaylistPosition(i);
        }

        audiosRepository.replaceAllByVkUserId(currentAppUser.getUserToken().getVkUserId(),
                audios);
        return audios;
      }
    }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe(vkAudiosSubscriber);
  }

  private class VkAudiosSubscriber extends Subscriber<List<Audio>> {
    @Override
    public void onCompleted() {
    }

    @Override
    public void onError(Throwable e) {
      vkAudiosSubscriber = null;
      view.hideRefreshLayout();
      Timber.e(e, "An error occurred while trying to get audios (username = '%s')",
              currentAppUser.getUsername());
      if (e instanceof IOException) {
        Timber.e("No network?");
        view.showNetworkErrorOccurredError();
      } else if (e instanceof VkException) {
        Timber.e("Unknown subclass of vk exception caught.");
      } else {
        throw Exceptions.propagate(e);
      }
    }

    @Override
    public void onNext(List<Audio> audios) {
      vkAudiosSubscriber = null;
      setCache(audios);
      view.hideRefreshLayout();
      showAudios();
    }
  }

  @Override
  public void onDownloadRequest(Audio audio) {
    audioDownloader.post(audio);
  }

  @Override
  public void onSearch(String query) {
    searchQuery = query;
    showAudios();
  }

  @Override
  public void onSearchCanceled() {
    searchQuery = "";
    showAudios();
  }

  @Override
  public void onDownloadSelectedAudiosRequest() {
    for(Audio audio : selectedInActionMode) {
      audioDownloader.post(audio);
      view.cancelAudioSelect(audio);
    }
    selectedInActionMode.clear();
  }

  @Override public void onSelectModeFinished() {
    for(Audio audio : selectedInActionMode) {
      view.cancelAudioSelect(audio);
    }
    selectedInActionMode.clear();
    startNormalMode();
  }

  @Override public void onAudioClicked(Audio audio) {
    mode.onAudioClicked(audio);
  }

  @Override public void onAudioLongClicked(Audio audio) {
    mode.onAudioLongClicked(audio);
  }

  private interface Mode {
    void onAudioClicked(Audio audio);
    void onAudioLongClicked(Audio audio);
  }

  private class SelectMode implements Mode {
    @Override public void onAudioClicked(Audio audio) {
      if (shouldBeSelected(audio)) {
        onAudioSelected(audio);
      } else {
        onAudioSelectCanceled(audio);
      }
    }

    @Override public void onAudioLongClicked(Audio audio) {
      onAudioClicked(audio);
    }
  }

  private boolean shouldBeSelected(Audio audio) {
    return !selectedInActionMode.contains(audio);
  }

  private void onAudioSelected(Audio audio) {
    selectedInActionMode.add(audio);
    view.showAudioSelected(audio);
    updateSelectModeTitle();
  }

  private void onAudioSelectCanceled(Audio audio) {
    selectedInActionMode.remove(audio);
    view.cancelAudioSelect(audio);
    updateSelectModeTitle();
    if (needToFinishSelectMode()) {
      view.finishSelectMode();
      startNormalMode();
    }
  }

  private boolean needToFinishSelectMode() {
    return selectedInActionMode.isEmpty();
  }

  private void startNormalMode() {
    this.mode = normalMode;
  }

  private void updateSelectModeTitle() {
    view.setSelectModeTitle(Collections.unmodifiableList(selectedInActionMode));
  }

  private class NormalMode implements Mode {
    @Override public void onAudioClicked(Audio audio) {
      view.showActionsFor(audio);
    }

    @Override public void onAudioLongClicked(Audio audio) {
      startSelectModeWith(audio);
    }
  }

  private void startSelectModeWith(Audio audio) {
    startSelectMode();
    onAudioSelected(audio);
  }

  private void startSelectMode() {
    mode = selectMode;
    view.startSelectMode();
  }
}
