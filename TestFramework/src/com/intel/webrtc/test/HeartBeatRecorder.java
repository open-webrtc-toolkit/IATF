package com.intel.webrtc.test;

import java.util.Hashtable;

/**
 * Manage the received heart beat messages automatically.
 * It specifically maintains the oldest heart beat.
 * @author xianglai
 *
 */
public class HeartBeatRecorder {

    // *************************************************************************
    // HeartBeatRecorder keeps records on input heart beats, and put them in a
    // minimum heap, so that it is easy to get the oldest heart beat, and check
    // whether it has been timeout.
    // To update the heart beat record quickly, it also keeps a HashTable for
    // Device-TimeStamp mapping.
    // If HeartBeatRecorder receives a new heart beat message from one device,
    // it will find the Previous TimeStamp of the device via the HashTable,
    // and update the time, and then maintain the minimum heap.
    // *************************************************************************
    final static private String TAG = "HeartBeatRecorder";

    /**
     * The data structure of a time stamp of heart beat.
     * @author xianglai
     *
     */
    class TimeStamp {
        private String deviceName;
        private long time;
        private int index;

        /**
         * Create a time stamp for a new device
         * @param deviceName
         *    the device name.
         * @param time
         *    time in millisecond
         * @param index
         *    the index of this TimeStamp object in the ArrayList.
         *
         */
        TimeStamp(String deviceName, int time, int index) {
            this.deviceName = deviceName;
            this.time = time;
            this.index = index;
        }

        public String getDeviceName() {
            return deviceName;
        }

        public long getTime() {
            return time;
        }

        public void setTime(long newTime) {
            if (time < newTime)
                time = newTime;
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int newIndex) {
            index = newIndex;
        }

        /**
         * Check whether the time of this time stamp is earlier
         * than the input TimeStamp.
         * @param timeStamp
         *     the TimeStamp comparing to.
         * @return
         *     true, if this time stamp is earlier.
         *     false, if this time stamp is later.
         *
         */
        public boolean isEarlierThan(TimeStamp timeStamp) {
            if (time < timeStamp.getTime())
                return true;
            else
                return false;
        }

        /**
         * Check whether the time of this time stamp is later
         * than the input TimeStamp.
         * @param timeStamp
         *     the TimeStamp comparing to.
         * @return
         *     true, if this time stamp is later.
         *     false, if this time stamp is earlier.
         *
         */
        public boolean isLaterThan(TimeStamp timeStamp) {
            if (time > timeStamp.getTime())
                return true;
            else
                return false;
        }

        /**
         * Check whether the time of this time stamp is equal to
         * the input TimeStamp.
         */
        public boolean equals(TimeStamp timeStamp) {
            if (time == timeStamp.getTime())
                return true;
            else
                return false;
        }
    }

    Hashtable<String, TimeStamp> indexTable;
    TimeStamp[] timeStamps; // A minimum heap, starts at index 1.

    /**
     * Create a new HeartBeatRecorder, and all time will be initialized as the current time.
     * TODO the value should be current time, rather than -1
     * @param devices
     *   the names of the test devices
     */
    public HeartBeatRecorder(String[] devices) {
        int numOfDevices = devices.length;
        timeStamps = new TimeStamp[numOfDevices + 1];
        indexTable = new Hashtable<String, TimeStamp>();
        for (int i = 0, j = 1; i < numOfDevices; i++, j++) {
            TimeStamp timeStamp = new TimeStamp(devices[i], -1, j);
            timeStamps[j] = timeStamp;
            indexTable.put(devices[i], timeStamp);
        }
    }

    /**
     * get the TimeStamp of the earliest heart beat
     * @return
     */
    public TimeStamp getEarliest() {
        return timeStamps[1];
    }

    private void swap(int index1, int index2) {
        TimeStamp temp = timeStamps[index1];
        timeStamps[index1] = timeStamps[index2];
        timeStamps[index2] = temp;
        timeStamps[index1].setIndex(index1);
        timeStamps[index2].setIndex(index2);
    }

    /**
     * To maintain the minimum heap
     * @param index
     */
    private void down(int index) {
        int leftChild = index * 2, rightChild = leftChild + 1;
        if (leftChild >= timeStamps.length)
            return;
        if (rightChild >= timeStamps.length) {
            if (timeStamps[leftChild].isEarlierThan(timeStamps[index])) {
                swap(index, leftChild);
                down(leftChild);
            }
            return;
        }
        if (timeStamps[rightChild].isEarlierThan(timeStamps[leftChild])) {
            if (timeStamps[rightChild].isEarlierThan(timeStamps[index])) {
                swap(index, rightChild);
                down(rightChild);
            }
        } else {
            if (timeStamps[leftChild].isEarlierThan(timeStamps[index])) {
                swap(index, leftChild);
                down(leftChild);
            }
        }
    }

    /**
     * Input a new coming heart beat.
     * @param device
     *     the device that the heart beat from
     * @param time
     *     the time in millisecond
     */
    public void onHeartBeat(String device, long time) {
        if (indexTable.containsKey(device)) {
            synchronized (timeStamps) {
                int index = indexTable.get(device).getIndex();
                timeStamps[index].setTime(time);
                down(index);
                Logger.d(TAG, "New heartbeat,Device:" + device + "\ttime :"
                        + (time - 1430895891722L));
                Logger.d(TAG, "Oldest heartbeat,Device:"
                        + getEarliest().deviceName + "\ttime :"
                        + (getEarliest().time - 1430895891722L));
            }
        }
    }
}
