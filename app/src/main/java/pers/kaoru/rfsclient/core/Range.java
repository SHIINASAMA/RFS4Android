package pers.kaoru.rfsclient.core;

public class Range {

    private final Long begin;
    private final Long end;
    private final Long total;

    public Range(Long begin, Long end, Long total) {
        this.begin = begin;
        this.end = end;
        this.total = total;
    }

    public Long getBegin() {
        return begin;
    }

    public Long getEnd() {
        return end;
    }

    public Long getTotal() {
        return total;
    }

    @Override
    public String toString() {
        return begin + "-" + end + "/" + total;
    }

    public static Range RangeBuild(String string) {
        long total;
        long begin;
        long end;
        String[] strings = string.split("/");
        assert strings.length == 2;
        total = Long.parseLong(strings[1]);
        strings = strings[0].split("-");
        assert strings.length == 2;
        begin = Long.parseLong(strings[0]);
        end = Long.parseLong(strings[1]);
        return new Range(begin, end, total);
    }
}
