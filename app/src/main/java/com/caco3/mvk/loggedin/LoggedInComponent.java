package com.caco3.mvk.loggedin;

import com.caco3.mvk.audios.AudiosComponent;
import com.caco3.mvk.audios.AudiosModule;
import com.caco3.mvk.navdrawer.NavDrawerComponent;
import com.caco3.mvk.navdrawer.NavDrawerModule;

import dagger.Subcomponent;

/**
 * Has an active {@link com.caco3.mvk.data.appuser.AppUser}
 */
@LoggedInScope
@Subcomponent(
        modules = {
                LoggedInModule.class
        }
)
public interface LoggedInComponent {
  AudiosComponent plus(AudiosModule audiosModule);
  NavDrawerComponent plus(NavDrawerModule navDrawerModule);
}
