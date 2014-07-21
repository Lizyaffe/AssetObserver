package com.masterface.nxt.ae;

import java.util.Comparator;
import java.util.Map;

public class ConfigurableComparator implements Comparator<Map<String, Object>> {

    private String[] sortOrder;
    private boolean isNumeric = true;

    public ConfigurableComparator(String config) {
        sortOrder = config.split(",");
    }

    @Override
    public int compare(Map<String, Object> o1, Map<String, Object> o2) {
        for (String sortKey : sortOrder) {
            boolean isDescending = false;
            if (sortKey.startsWith("-")) {
                isDescending = true;
                sortKey = sortKey.substring(1);
            }
            if (!(o1.get(sortKey) instanceof Comparable) || !(o2.get(sortKey) instanceof Comparable)) {
                throw new IllegalArgumentException(sortKey + " is not a legal field name");
            }
            if (o1.get(sortKey) instanceof Comparable<?>) {
                Comparable c1 = (Comparable) o1.get(sortKey);
                Comparable c2 = (Comparable) o2.get(sortKey);
                int rc;
                try {
                    if (isNumeric && c1 instanceof String && c2 instanceof String) {
                        Double d1 = Double.parseDouble((String) c1);
                        Double d2 = Double.parseDouble((String) c2);
                        if (isDescending) {
                            rc = d2.compareTo(d1);
                        } else {
                            rc = d1.compareTo(d2);
                        }
                        if (rc == 0) {
                            continue;
                        }
                        return rc;
                    }
                } catch (NumberFormatException ignore) {
                    // ignore and perform default comparison
                    isNumeric = false;
                }
                if (isDescending) {
                    rc = c2.compareTo(c1);
                } else {
                    rc = c1.compareTo(c2);
                }
                if (rc == 0) {
                    continue;
                }
                return rc;
            }
        }
        return 0;
    }
}
