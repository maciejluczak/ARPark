package pl.lednica.arpark.object_recognition_engine;
/*===============================================================================
Copyright (c) 2016 PTC Inc. All Rights Reserved.

Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of PTC Inc., registered in the United States and other
countries.
===============================================================================*/

import com.vuforia.State;


public interface CustomApplicationControl
{
    boolean doInitTrackers();

    boolean doLoadTrackersData();


    boolean doStartTrackers();


    boolean doStopTrackers();

    boolean doUnloadTrackersData();

    boolean doDeinitTrackers();

    void onInitARDone(CustomApplicationException e);

    void onVuforiaUpdate(State state);

}
