package com.weisser.quote.portfolio.cla;

/**
 * CSet is a set that keeps its elements in sorted order.
 *
 * TODO Migrate to a standard JDK Collections interface like SortedSet<Integer>
 * TODO Errors, when optimizing a problem with only one security.
 */
public class CSet {
    private int[] items;

    private int count;

    public CSet() {
    }

    /**
     * Sets the maximum number of elements in this CSet.
     * @param maxCount the maximum number of elements in this CSet.
     */
    public CSet(int maxCount) {
        initialize(maxCount);
    }

    /**
     * Initializes the CSet.
     * @param maxCount the maximum number of elements in this CSet.
     */
    public final void initialize(int maxCount) {
        items = new int[maxCount];
        count = 0;
    }

    public int count() {
        return count;
    }

    public void resize(int newsize) {
        items = Utility.redim(items, newsize);
    }

    public int elementAt(int index) {
        return items[index];
    }

    public void deleteAt(int index) {
        count--;
        if (count - index >= 0) System.arraycopy(items, index + 1, items, index, count - index);
    }

    /**
     * Inserts the item into the set while keeping the order intact.
     */
    public void add(int newitem) {
        int i;
        for (i = count; i >= 1; i--) {
            if (newitem > items[i - 1]) {
                break;
            } else {
                items[i] = items[i - 1];
            }
        }
        // System.out.println("Set("+i+") to "+ member);
        items[i] = newitem;
        count++;
    }

    /**
     * Deletes the given element from the set.
     *
     * @param item The element to be deleted.
     */
    public void delete(int item) {
        int i = position(item);
        deleteAt(i);
    }

    /**
     * Returns the index of the item in the array.
     *
     * @param item the member for which to get the position
     * @return the position in the array.
     */
    public int position(int item) {
        int i;

        for (i = 0; i < count; i++) {
            if (items[i] == item) {
                return i;
            }
        }

        // Should not happen...
        System.err.println("Unhandled error: CSet.position() - element not found.");
        return i;  // Better return -1 ?;
    }

    /**
     * Returns a string representation of this object.
     *
     * @return A string representation of this object.
     */
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("CSet: count=" + count + " [");

        for (int i = 0; i < count; i++) {
            s.append(items[i]).append(" ");
        }
        s.append("]");

        return s.toString();
    }
}
