package com.intel.webrtc.test;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

/**
 * Be responsible for the management of the remote wait-notify mechanism.
 * @author xianglai
 *
 */
public class WaitNotifyManager {
    private Hashtable<String, List<String>> waitingLists;

    public WaitNotifyManager() {
        waitingLists = new Hashtable<String, List<String>>();
    }

    /**
     * A device wait for a remote object.
     * The device will be added into the waiting list.
     *
     * @param deviceId
     *      the name of the device
     * @param objectId
     *      the name of the remote object
     */
    public void waitForObject(String deviceId, String objectId) {
        List<String> waitingList = null;
        synchronized (waitingLists) {
            // If the lock object exists, get it.
            // Otherwise, create a new object.
            if (waitingLists.containsKey(objectId)) {
                waitingList = waitingLists.get(objectId);
            } else {
                waitingList = new LinkedList<String>();
                waitingLists.put(objectId, waitingList);
            }
        }
        synchronized (waitingList) {
            if (!waitingList.contains(deviceId))
                waitingList.add(deviceId);
        }
    }

    /**
     * Notify a remote object.
     * WaitNotifyManager will notify the first device of the waiting list of
     * the remote object, and remove it from the list.
     *
     * @param objectId
     *      the name of the remote object
     */
    public String notifyObject(String objectId) {
        List<String> waitingList = null;
        synchronized (waitingLists) {
            if (waitingLists.containsKey(objectId)) {
                waitingList = waitingLists.get(objectId);
            } else {
                return null;
            }
        }
        synchronized (waitingList) {
            if (!waitingList.isEmpty()) {
                return waitingList.remove(0);
            }
            return null;
        }
    }

    /**
     * Notify a remote object.
     * WaitNotifyManager will notify all devices of the waiting list of
     * the remote object, and remove them from the list.
     *
     * @param objectId
     *      the name of the remote object
     */
    public String[] notifyObjectForAll(String objectId) {
        List<String> waitingList = null;
        synchronized (waitingLists) {
            if (waitingLists.containsKey(objectId)) {
                waitingList = waitingLists.get(objectId);
            } else {
                return null;
            }
        }
        synchronized (waitingList) {
            if (!waitingList.isEmpty()) {
                int n = waitingList.size();
                String[] result = new String[n];
                for (int i = 0; i < n; i++) {
                    result[i] = waitingList.remove(0);
                }
                return result;
            }
        }
        return null;
    }
}
