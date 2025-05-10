package org.example.capstonee;
import java.util.Map;
import java.util.HashMap;

public class FurInputHandler {
    private int size;
    private Map<Character, Boolean> keys;
    private Map<Character, Boolean> lockList;
    private final Character[] DEFAULT_VALUES = {'d','f','j','k',' ', 's', 'l'};

    public FurInputHandler() {
        this(4);
    }
    public FurInputHandler(int size) {
        this.size = size;
        generateDict();

    }
    private void generateDict() {
        keys = new HashMap<>();
        lockList = new HashMap<>();
        for(int i = 0; i < size; i++) {
            keys.put(DEFAULT_VALUES[i], false);
            lockList.put(DEFAULT_VALUES[i], false);
        }
    }
    public void press(char k) {
        if(keys.containsKey(k) && !keys.get(k)) {
            keys.put(k, true);
        }
    }

    public void lock(char k) {
        if(keys.containsKey(k) && !lockList.get(k)) {
            lockList.put(k, true);
        }
    }

    public void release(char k) {
        if(keys.containsKey(k) && keys.get(k)) {
            keys.put(k, false);
        }
    }

    public void unlock(char k) {
        if(keys.containsKey(k) && lockList.get(k)) {
            lockList.put(k, false);
        }
    }

    public boolean getValue(char k) {
        return keys.containsKey(k) ? keys.get(k) : false;
    }

    public boolean getLock(char k) {
        return lockList.containsKey(k) ? lockList.get(k) : false;
    }

    public int getSize() {
        return this.size;
    }
    public void setSize(int size) {
        this.size = size;
        generateDict();
    }

    public char[] getKeys() {
        char[] tempKeys = new char[size];
        for(int i = 0; i < size; i++) {
            tempKeys[i] = DEFAULT_VALUES[i];
        }
        return tempKeys;
    }

    public void destroy() {
        keys.clear();
        lockList.clear();
        keys = null;
        lockList = null;
    }

}
