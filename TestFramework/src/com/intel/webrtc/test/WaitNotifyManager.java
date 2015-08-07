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
    private Hashtable<String, Boolean> notifyLists;

    public WaitNotifyManager() {
        waitingLists = new Hashtable<String, List<String>>();
        notifyLists = new Hashtable<String, Boolean>();
    }

    /**
     * A device wait for a remote object. The device will be added into the
     * waiting list.
     *
     * @param deviceId
     *            the name of the device
     * @param objectId
     *            the name of the remote object
     * @return if related notify message has arrived already. True, arrived
     * and notifyObject should be called; False, not arrived yet, the message
     * should be sent into the waiting list.
     */
    public boolean waitForObject(String deviceId, String objectId) {
        List<String> waitingList = null;
        Boolean notifyAll;
        boolean notifyArrived = false;
        synchronized (notifyLists) {
            // Check whether the notify information has already arrived
            if (notifyLists.containsKey(objectId)) {
                // list of notify locks
                notifyAll = notifyLists.get(objectId);
                if (!notifyAll) {
                    // notify one object, delete
                    notifyLists.remove(objectId);
                }
                notifyArrived = true;
                // no need to edit the waitingLists
                return notifyArrived;
            }
        }
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
        return notifyArrived;
    }

    /**
     * Notify a remote object. WaitNotifyManager will notify the first device of
     * the waiting list of the remote object, and remove it from the list.
     *
     * @param objectId
     *            the name of the remote object
     */
    public String notifyObject(String deviceId, String objectId) {
        List<String> waitingList = null;
        synchronized (waitingLists) {
            if (waitingLists.containsKey(objectId)) {
                waitingList = waitingLists.get(objectId);
            } else {
                // no waiting items, store notify in list
                notifyLists.put(objectId, false);
                return null;
            }
        }
        synchronized (waitingList) {
            if (!waitingList.isEmpty()) {
                // TODO: further optimization needed
                return waitingList.remove(0);
            }
            return null;
        }
    }

    /**
     * Notify a remote object. WaitNotifyManager will notify all devices of the
     * waiting list of the remote object, and remove them from the list.
     *
     * @param objectId
     *            the name of the remote object
     */
    public String[] notifyObjectForAll(String objectId) {
        // There maybe some wait items not arrived yet
        // Whenever notifyAll is arrived, should be stored
        notifyLists.put(objectId, true);
        List<String> waitingList = null;
        synchronized (waitingLists) {
            if (waitingLists.containsKey(objectId)) {
                waitingList = waitingLists.get(objectId);
            } else {
                // no waiting items, store notify in list
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
