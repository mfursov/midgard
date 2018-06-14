package midgard.json;

import org.jetbrains.annotations.Nullable;

class JSON {
    /**
     * Returns the input if it is a JSON-permissible value; throws otherwise.
     */
    static double checkDouble(double d) {
        if (Double.isInfinite(d) || Double.isNaN(d)) {
            throw new IllegalArgumentException("Forbidden numeric value: " + d);
        }
        return d;
    }

    @Nullable
    static Boolean toBoolean(@Nullable Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else if (value instanceof String) {
            String stringValue = (String) value;
            if ("true".equalsIgnoreCase(stringValue)) {
                return true;
            } else if ("false".equalsIgnoreCase(stringValue)) {
                return false;
            }
        }
        return null;
    }

    @Nullable
    static Double toDouble(@Nullable Object value) {
        if (value instanceof Double) {
            return (Double) value;
        } else if (value instanceof Number) {
            return ((Number) value).doubleValue();
        } else if (value instanceof String) {
            try {
                return Double.valueOf((String) value);
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    @Nullable
    static Integer toInteger(@Nullable Object value) {
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof Number) {
            return ((Number) value).intValue();
        } else if (value instanceof String) {
            try {
                return (int) Double.parseDouble((String) value);
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    @Nullable
    static Long toLong(@Nullable Object value) {
        if (value instanceof Long) {
            return (Long) value;
        } else if (value instanceof Number) {
            return ((Number) value).longValue();
        } else if (value instanceof String) {
            try {
                return (long) Double.parseDouble((String) value);
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    @Nullable
    static String toString(@Nullable Object value) {
        if (value instanceof String) {
            return (String) value;
        } else if (value != null) {
            return String.valueOf(value);
        }
        return null;
    }

    public static IllegalArgumentException typeMismatch(Object indexOrName, Object actual,
                                                        String requiredType) {
        if (actual == null) {
            throw new IllegalArgumentException("Value at " + indexOrName + " is null.");
        } else {
            throw new IllegalArgumentException("Value " + actual + " at " + indexOrName
                    + " of type " + actual.getClass().getName()
                    + " cannot be converted to " + requiredType);
        }
    }

    public static IllegalArgumentException typeMismatch(Object actual, String requiredType) {
        if (actual == null) {
            throw new IllegalArgumentException("Value is null.");
        } else {
            throw new IllegalArgumentException("Value " + actual
                    + " of type " + actual.getClass().getName()
                    + " cannot be converted to " + requiredType);
        }
    }
}
