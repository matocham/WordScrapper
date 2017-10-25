package dictionary.range;

/**
 * Created by Mateusz on 02.04.2017.
 */
public class Range {
    int from,to;

    public Range(int from, int to) {
        this.from = from;
        this.to = to;
    }

    public Range(String range){
        String ranges[] = range.split("-");
        from = Integer.parseInt(ranges[0]);
        to = Integer.parseInt(ranges[1]);
    }

    public boolean inRange(int value){
        return value>=from && value<=to;
    }
}
