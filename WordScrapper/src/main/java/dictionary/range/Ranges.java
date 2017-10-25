package dictionary.range;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mateusz on 02.04.2017.
 */
public class Ranges {
    List<Range> ranges;

    public Ranges(String rangesString){
        String[] rangesArray = rangesString.split(",");
        ranges = new ArrayList<>();

        for(String range :  rangesArray){
            ranges.add(new Range(range));
        }
    }

    public boolean isInAnyRange(int value){
        for(Range r : ranges){
            if(r.inRange(value)){
                return true;
            }
        }
        return false;
    }
}
