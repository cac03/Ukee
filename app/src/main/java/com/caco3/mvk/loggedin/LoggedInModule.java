package com.caco3.mvk.loggedin;

import com.caco3.mvk.data.appuser.AppUser;
import com.caco3.mvk.data.appuser.AppUsersRepository;
import com.caco3.mvk.vk.auth.UserToken;

import dagger.Module;
import dagger.Provides;


@Module
public class LoggedInModule {

  @Provides
  @LoggedInScope
  public AppUser provideAppUser(AppUsersRepository appUsersRepository) {
    return appUsersRepository.getActive();
  }

  @Provides
  @LoggedInScope
  public UserToken provideUserToken(AppUser appUser) {
    return appUser.getUserToken();
  }
}
