package pl.lednica.arpark.object_recognition_engine;
/*===============================================================================
Copyright (c) 2016 PTC Inc. All Rights Reserved.

Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of PTC Inc., registered in the United States and other
countries.
===============================================================================*/

import com.vuforia.State;


//  Interface to be implemented by the activity which uses SampleApplicationSession
// Interfejs dla aktywnosci dziedziczacych z SampleApplicationSession
public interface SampleApplicationControl
{

    // To be called to initialize the trackers
    // Do inicjalizacji Wykrywaczy
    boolean doInitTrackers();


    // To be called to load the trackers' data
    // Do zaladowania danych dla Wykrywaczy
    boolean doLoadTrackersData();


    // To be called to start tracking with the initialized trackers and their
    // loaded data
    // Odpalany kiedy zainicjowano Wykrywacze i dane, ktorych potrzebuja
    boolean doStartTrackers();


    // To be called to stop the trackers
    // Do zatrzymywania Wykrywaczy
    boolean doStopTrackers();


    // To be called to destroy the trackers' data
    // Do niszczenia danych Wykrywaczy
    boolean doUnloadTrackersData();


    // To be called to deinitialize the trackers
    // Do deinicjowania Wykrywaczy
    boolean doDeinitTrackers();


    // This callback is called after the Vuforia initialization is complete,
    // the trackers are initialized, their data loaded and
    // tracking is ready to start
    // Kiedy Vuforia wszystko ladnie zrobi
    void onInitARDone(SampleApplicationException e);


    // This callback is called every cycle
    // Opalane za kazdym cyklem ( klatka )
    void onVuforiaUpdate(State state);

}
