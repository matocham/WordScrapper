package matocham.comparator;

import java.util.Comparator;

/**
 * Created by Mateusz on 03.04.2017.
 */
public class NodeComparator  implements Comparator<NodeWrapper> {
    @Override
    public int compare(NodeWrapper o1, NodeWrapper o2) {
        return o1.getName().compareTo(o2.getName());
    }
}
