package com.caco3.mvk.audios;

import com.caco3.mvk.data.appuser.AppUser;
import com.caco3.mvk.data.audio.AudiosRepository;
import com.caco3.mvk.vk.Vk;

import dagger.Module;
import dagger.Provides;

@Module
public class MyAudiosModule {
  @Provides
  @MyAudiosScope
  public MyAudiosPresenter provideAudiosPresenter(AppUser appUser, AudiosRepository audiosRepository,
                                                  Vk vk) {
    return new MyAudiosPresenterImpl(appUser, audiosRepository, vk);
  }
}
