package com.example.demo;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class Util {
    public static boolean isBlank(String s) {
        return Strings.isBlank(s);
    }

    public static boolean isNotBlank(String s) {
        return Strings.isNotBlank(s);
    }

    public static <T> Iterable<List<T>> partition(Collection<T> source, int partition) {
        return Collections.partition(source, partition);
    }

    public static <T> boolean isEmpty(Collection<T> source) {
        return Collections.isEmpty(source);
    }

    public static <T> boolean isNotEmpty(Collection<T> source) {
        return Collections.isNotEmpty(source);
    }

    public static class Strings {
        public static boolean isBlank(String s) {
            return s == null || s.isBlank();
        }

        public static boolean isNotBlank(String s) {
            return !isBlank(s);
        }

        public static String capitalize(String s) {
            return s.substring(0, 0).toUpperCase() + s.substring(1);
        }

        public static String exception(Throwable t) {
            try (
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    PrintWriter printWriter = new PrintWriter(outputStream)
            ) {
                t.printStackTrace(printWriter);
                return outputStream.toString();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public static String trimLeadingZeros(String invoiceNo) {
            if (!invoiceNo.startsWith("0")) {
                return invoiceNo;
            }

            for (int i = 0; i < invoiceNo.length(); i++) {
                char c = invoiceNo.charAt(i);
                if (c != '0') {
                    return invoiceNo.substring(i);
                }
            }
            return invoiceNo;
        }
    }

    public static class Collections {
        public static <T> Iterable<List<T>> partition(Collection<T> source, int partition) {
            Iterator<T> sourceIterator = source.iterator();
            return () -> partition(sourceIterator, partition);
        }

        public static <T> Iterator<List<T>> partition(Iterator<T> iterator, int partition) {
            return new Iterator<>() {
                @Override
                public boolean hasNext() {
                    return iterator.hasNext();
                }

                @Override
                public List<T> next() {
                    List<T> result = new ArrayList<>(partition);
                    for (int i = 0; i < partition && iterator.hasNext(); i++) {
                        result.add(iterator.next());
                    }

                    return result;
                }
            };
        }

        public static <T> boolean isNotEmpty(Collection<T> source) {
            return !isEmpty(source);
        }

        public static <T> boolean isEmpty(Collection<T> source) {
            return source == null || source.isEmpty();
        }
    }

    public static class Dates {
        private static final DateTimeFormatter DTF_yyyyMMddHHmmSSzzz = DateTimeFormatter.ofPattern("yyyy-MM-dd['T']HH:mm:ss[.SSS]['Z']");
        private static final DateTimeFormatter DTF_yyyyMMdd = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        public static LocalDateTime tryToParseFull(String date) {
            return LocalDateTime.parse(date, DTF_yyyyMMddHHmmSSzzz);
        }

        public static String formatFull(LocalDateTime dateTime) {
            return dateTime.format(DTF_yyyyMMddHHmmSSzzz);
        }

        public static LocalDateTime localDateTime(String date) {
            try {
                return LocalDateTime.parse(date, DTF_yyyyMMddHHmmSSzzz);
            } catch (Exception e) {
                return null;
            }
        }

        public static LocalDate localDate(String date) {
            LocalDateTime localDateTime = localDateTime(date);
            if (localDateTime != null) {
                return localDateTime.toLocalDate();
            }

            try {
                return LocalDate.parse(date, DTF_yyyyMMdd);
            } catch (Exception e) {
                return null;
            }
        }

        public static String formatFull(String date) {
            if (Strings.isBlank(date)) {
                return null;
            }
            LocalDateTime dateTime = tryToParseFull(date);
            return formatFull(dateTime);
        }
    }

    public static class BigDecimals {
        public static BigDecimal parse(String v, int scale) {
            if (Strings.isBlank(v)) {
                return null;
            }
            v = v.replace(",", "");
            return new BigDecimal(v).setScale(scale, RoundingMode.HALF_UP);
        }

        public static BigDecimal parseScale4(String v) {
            return parse(v, 4);
        }
    }

    public static class Integers {
        public static Integer parse(String v) {
            if (Strings.isBlank(v)) {
                return null;
            }
            v = v.replace(",", "");
            return Integer.parseInt(v);
        }
    }
}
