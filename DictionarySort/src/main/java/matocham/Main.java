package matocham;

import matocham.builder.XmlExportBuilder;
import matocham.comparator.NodeComparator;
import matocham.comparator.NodeWrapper;
import matocham.loader.XmlLoader;
import org.w3c.dom.Node;

import java.util.Collections;
import java.util.List;

public class Main {

    private static boolean removeRedundant = false;

    public static void main(String[] args) {
        if (args.length < 2) {
            throw new RuntimeException("Wrong arguments. <source> <destination> [-r (remove redundant)]");
        }
        String source = args[0];
        String destination = args[1];
        if (args.length > 2) {
            if (args[2].equals("-r")) {
                removeRedundant = true;
            }
        }
        long startTime = System.currentTimeMillis();
        int skipped = 0;
        System.out.println("Loading document...");
        List<NodeWrapper> nodes = XmlLoader.getNodesAsList(XmlLoader.getDictionaryEntries(source));
        System.out.println("Sorting ...");
        Collections.sort(nodes, new NodeComparator());
        System.out.println("Creating file representation ...");
        XmlExportBuilder builder = new XmlExportBuilder();
        builder.startDocument();
        NodeWrapper previous = null;
        for (NodeWrapper node : nodes) {
            Node n = node.getNode();
            if (removeRedundant && node.equals(previous)) {
                System.out.println("Skipping "+node.getName()+" because already exists");
                skipped++;
                continue;
            }
            builder.appendNode(n);
            previous = node;
        }
        System.out.println("Exporting doucment ...");
        builder.export(destination);
        System.out.println("Execution time: "+(System.currentTimeMillis()-startTime)/1000+" seconds");
        System.out.println("Skipped redundant words: "+skipped);
    }
}
