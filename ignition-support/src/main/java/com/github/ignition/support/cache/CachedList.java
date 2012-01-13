package com.github.ignition.support.cache;

import java.io.IOException;
import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Superclass of all list objects to be stored in {@link ModelCache}.
 * 
 * Operates just as standard cached object, and contains an array list of objects.
 * 
 * <b>Must</b> be initialized with the class of the objects stored, as this is used in
 * parcelling/unparcelling.
 * 
 * In order to ensure thread-safe use of list (such as iteration), use the {@link #getList()}
 * method, creating a copy of the list in its current state.
 * 
 * @author michaelengland
 * 
 * @param <CO>
 *            Type of cached models to be stored in list
 */
public class CachedList<CO extends CachedModel> extends CachedModel {

    /**
     * Class type of object list
     */
    protected Class<? extends CachedModel> clazz;
    /**
     * List of objects.
     */
    protected ArrayList<CO> list;

    /**
     * Simple parameter-less constructor. <b>Must</b> also have parameter-less constructor in
     * subclasses in order for parceling to work.
     * 
     * <b>Do not use this constructor when creating a list, use one setting class instead.</b>
     */
    public CachedList() {
        list = new ArrayList<CO>();
    }

    /**
     * Constructor setting variables from parcel. Same as using a blank constructor and calling
     * readFromParcel.
     * 
     * @param source
     *            Parcel to be read from.
     * @throws IOException
     */
    public CachedList(Parcel source) throws IOException {
        super(source);
    }

    /**
     * Constructor initializing class of objects stored.
     * 
     * @param clazz
     *            Required for parcelling and unparcelling of list
     */
    public CachedList(Class<? extends CachedModel> clazz) {
        this.clazz = clazz;
        list = new ArrayList<CO>();
    }

    /**
     * Constructor initializing class of objects stored as well as initial length of list.
     * 
     * @param clazz
     *            Required for parcelling and unparcelling of list
     * @param initialLength
     *            Initial length of list
     */
    public CachedList(Class<? extends CachedModel> clazz, int initialLength) {
        this.clazz = clazz;
        list = new ArrayList<CO>(initialLength);
    }

    /**
     * Constructor initializing class of objects stored as well as id used in key generation.
     * 
     * @param clazz
     *            Required for parcelling and unparcelling of list
     * @param id
     *            ID of new list (used when generating cache key).
     */
    public CachedList(Class<? extends CachedModel> clazz, String id) {
        super(id);
        this.clazz = clazz;
        list = new ArrayList<CO>();
    }

    /**
     * Synchronized method to get a copy of the list in its current state. This should be used when
     * iterating over the list in order to avoid thread-unsafe operations.
     * 
     * @return Copy of list in its current state
     */
    public synchronized ArrayList<CO> getList() {
        return new ArrayList<CO>(list);
    }

    /**
     * Synchronized method used to append an object to the list.
     * 
     * @param cachedObject
     *            Object to add to list
     */
    public synchronized void add(CO cachedObject) {
        list.add(cachedObject);
    }

    /**
     * Synchronized method used to set an object at a location in the list.
     * 
     * @param index
     *            Index of item to set
     * @param cachedObject
     *            Object to set in list
     */
    public synchronized void set(int index, CO cachedObject) {
        list.set(index, cachedObject);
    }

    /**
     * Synchronized method used to get an object from the live list.
     * 
     * @param index
     *            Index of item in list
     * @return Item in list
     */
    public synchronized CO get(int index) {
        return list.get(index);
    }

    /**
     * Synchronized method used to return size of list.
     * 
     * @return Size of list
     */
    public synchronized int size() {
        return list.size();
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public synchronized boolean equals(Object o) {
        if (!(o instanceof CachedList)) {
            return false;
        }
        @SuppressWarnings("rawtypes")
        CachedList that = (CachedList) o;
        return clazz.equals(that.clazz) && list.equals(that.list);
    }

    /**
     * @see com.github.droidfu.cachefu.CachedModel#createKey(java.lang.String)
     */
    @Override
    public synchronized String createKey(String id) {
        return "list_" + id;
    }

    @Override
    public boolean reload(ModelCache modelCache) {
        // First reload list object
        boolean result = super.reload(modelCache);
        // Then reload each item in list. Sometimes a ConcurrentModificationException occurs.
        // Changed implementation so that it doesn't use an Iterator any more.
        // Uglier but hopefully that will solve the issue.
        for (int i = 0; i < list.size(); i++) {
            CachedModel listModel = list.get(i);
            if (listModel.reload(modelCache)) {
                result = true;
            }
        }
        return result;
    }

    /**
     * @see com.github.droidfu.cachefu.CachedModel#reloadFromCachedModel(com.github.droidfu.cachefu.ModelCache,
     *      com.github.droidfu.cachefu.CachedModel)
     */
    @Override
    public synchronized boolean reloadFromCachedModel(ModelCache modelCache, CachedModel cachedModel) {
        @SuppressWarnings("unchecked")
        CachedList<CO> cachedList = (CachedList<CO>) cachedModel;
        clazz = cachedList.clazz;
        list = cachedList.list;
        return false;
    }

    /**
     * Creator object used for parcelling
     */
    public static final Creator<CachedList<CachedModel>> CREATOR = new Parcelable.Creator<CachedList<CachedModel>>() {

        /**
         * @see android.os.Parcelable.Creator#createFromParcel(android.os.Parcel)
         */
        @SuppressWarnings({ "rawtypes", "unchecked" })
        @Override
        public CachedList<CachedModel> createFromParcel(Parcel source) {
            try {
                return new CachedList(source);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        /**
         * @see android.os.Parcelable.Creator#newArray(int)
         */
        @SuppressWarnings("unchecked")
        @Override
        public CachedList<CachedModel>[] newArray(int size) {
            return new CachedList[size];
        }

    };

    /**
     * @see com.github.droidfu.cachefu.CachedModel#readFromParcel(android.os.Parcel)
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void readFromParcel(Parcel source) throws IOException {
        super.readFromParcel(source);
        // Read class from parcel, then load class and use creator to generate new object from data
        String className = source.readString();
        try {
            clazz = (Class<? extends CachedModel>) Class.forName(className);
            list = source.createTypedArrayList((Creator) clazz.getField("CREATOR").get(this));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @see com.github.droidfu.cachefu.CachedModel#writeToParcel(android.os.Parcel, int)
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        // Write class name to parcel before object, so can be loaded correctly back in
        dest.writeString(clazz.getCanonicalName());
        dest.writeTypedList(list);
    }

}
