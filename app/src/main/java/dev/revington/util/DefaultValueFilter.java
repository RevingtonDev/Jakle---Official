package dev.revington.util;

public class DefaultValueFilter {

    @Override
    public boolean equals(Object obj) {
        if(obj == null) return true;

        if (obj instanceof String)
            return obj.toString().equals("");

        if(obj instanceof Integer || obj instanceof Short || obj instanceof Character || obj instanceof Long)
            return obj instanceof Long ? (long) obj == 0 : (int) obj == 0;

        if(obj instanceof Float || obj instanceof Double)
            return (double)obj == 0;

        if(obj instanceof Boolean)
            return !((Boolean) obj);

        return false;
    }
}
