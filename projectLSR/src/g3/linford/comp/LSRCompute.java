package g3.linford.comp;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

class Vertex implements Comparable<Vertex>{
    private final char name;
    protected ArrayList<Edge> adjacency;
    protected int minDistance = Integer.MAX_VALUE;
    protected Vertex previous;

    public Vertex(char argName){
        name = argName;
        adjacency = new ArrayList<Edge>();
    }
    public String getName(){
        return name+"";
    }
    public char getName0(){
        return name;
    }
    public String toString (){return name+"";}
    public int compareTo(Vertex other){
        return Integer.compare(minDistance, other.minDistance);
    }
}

class Edge{
    private final Vertex target;
    private final int cost;

    public Edge(Vertex argTarget, int argCost){
        target = argTarget; cost = argCost;
    }
    public Vertex getTarget(){return this.target;}
    public int getCost(){return cost;}
    public String getTargetName(){return target.getName();}
    public char getTargetName0(){return target.getName0();}
    public String toString(){return target.getName()+": "+cost;}
}

// for sorting
class ComparatorByName implements Comparator<Vertex> {
    @Override
    public int compare(Vertex v1, Vertex v2){
        return v1.getName0()-v2.getName0();
    }
}

public class LSRCompute {

    public static void main(String[] args) {
        HashMap<Character, Integer> vertMap = new HashMap<Character, Integer>();
        ArrayList<Vertex> allVertices = new ArrayList<Vertex>();

        try{
            Path inputPath = Paths.get("route.lsa");
            //Path inputPath = Paths.get(args[0]);
            int index0 = 0;
            BufferedReader br = Files.newBufferedReader(inputPath);
            String strLine;

            while ((strLine = br.readLine()) != null){

                String[] aLineOfRecord = strLine.split("\\s+"); // split by spaces store in aLineOfRecord
                char curNodeName = aLineOfRecord[0].charAt(0);
                allVertices.add(new Vertex(curNodeName));
                vertMap.put(curNodeName,index0);

                // create neighbors
                for(int neighbors=1; neighbors<aLineOfRecord.length; neighbors++){ // start at 1, exclude 0(newNode itself)
                    char nb =  (aLineOfRecord[neighbors]).charAt(0); // take out the neighbor
                    int cost = Integer.parseInt( aLineOfRecord[neighbors].substring(2));  // take the neighbor cost
                    for (Vertex ind : allVertices){
                        if (ind.getName0()==(nb)){
                            allVertices.get(index0).adjacency.add( new Edge(ind, cost) );  // add into neighbor
                            ind.adjacency.add(new Edge(allVertices.get(vertMap.get(curNodeName)), cost));
                            //System.out.println(ind.adjacency);
                        }
                    }
                }
                index0++;
            }
            //Close the input stream
            br.close();
        }catch(Exception e){//Catch exception if any
            System.err.println("Error in reading file: " + e.getMessage());
            System.exit(1);
        }

        int src = -1;
        char srcName = 't' ;
        //char srcName = args[1].charAt(0) ;
        System.out.println(allVertices);
        String mode = "SS";//default mode
        //if (args[2].toUpperCase().equals("CA")||args[2].toUpperCase().equals("SS"))mode = args[2].toUpperCase();

        while(true){
            for (int i=0;i<allVertices.size();i++){
                if(allVertices.get(i).getName0()==srcName){
                    src = i;
                }
            }
            if(src==-1){
                System.out.println("Source node not found");
                System.exit(1);
            }
            switch( mode ) {
                case "CA":  // compute all
                {
                    computeAllPaths(allVertices.get(src));
                    break;
                }

                case "SS":  // run step by step
                {
                    computePathsBySteps(allVertices.get(src), allVertices.toArray().length);
                    break;
                }
            }
            System.out.println("Source " + allVertices.get(src).getName() + ":");
            printShortestPaths(allVertices,src);
            displayAllRelations(allVertices);

            allVertices=modifyNodes(allVertices);
            allVertices=delEdge(allVertices);
            exitOrNot();
            for (Vertex allNodes : allVertices) { // reset all the distance values
                allNodes.minDistance = Integer.MAX_VALUE;
            }
            pressEnterKeyToContinue();
        }
    }

    public static void computeAllPaths(Vertex source) {
        source.minDistance = 0;
        PriorityQueue<Vertex> vertexQueue = new PriorityQueue<Vertex>();
        vertexQueue.add(source);

        while (!vertexQueue.isEmpty()) {
            Vertex u = vertexQueue.poll();
            for (Edge e : u.adjacency){
                Vertex v = e.getTarget();
                int cost = e.getCost();
                int distanceThroughU = u.minDistance + cost;
                if (distanceThroughU < v.minDistance) {
                    vertexQueue.remove(v);
                    v.minDistance = distanceThroughU ;
                    v.previous = u;
                    vertexQueue.add(v);
                }
            }
        }
    }

    private static void computePathsBySteps(Vertex source, int length) {
        List visitedNodes = new ArrayList<Vertex>(length);
        visitedNodes.add(source);
        source.minDistance = 0;
        PriorityQueue<Vertex> vertexQueue = new PriorityQueue<Vertex>();
        vertexQueue.add(source);

        while (!vertexQueue.isEmpty()) {
            Vertex u = vertexQueue.poll();
            for (Edge e : u.adjacency){
                Vertex v = e.getTarget();
                int cost = e.getCost();
                int distanceThroughU = u.minDistance + cost;
                if (distanceThroughU < v.minDistance) {
                    vertexQueue.remove(v);
                    v.minDistance = distanceThroughU ;
                    v.previous = u;
                    if((!visitedNodes.contains(v)) ){
                        visitedNodes.add(v);
                        System.out.print( "Found "+v.getName() + ": " );
                        List<Vertex> path = getShortestPathTo(v);
                        System.out.print("Path: ");
                        for(int i = 0; i < path.size(); i++) {
                            System.out.print( path.get(i).getName());
                            if (i<path.size()-1)
                                System.out.print( ">" );
                        }
                        System.out.println(" Cost:"+ v.minDistance);
                        pressEnterKeyToContinue();
                    }
                    vertexQueue.add(v);
                }
            }
        }
        //System.out.println(visitedNodes);
    }

    public static List<Vertex> getShortestPathTo(Vertex target) {
        List<Vertex> res = new ArrayList<Vertex>();
        for (Vertex vertex = target; vertex != null; vertex = vertex.previous)
            res.add(vertex);
        Collections.reverse(res);
        return res;
    }

    public static void printShortestPaths(ArrayList<Vertex> vertices, int src){
        for (Vertex v : vertices) {  // print out all the shortest paths of each node
            if (v != vertices.get(src)) {
                System.out.print(v.getName() + ": ");
                List<Vertex> path = getShortestPathTo(v);
                System.out.print("Path: ");
                for (int i = 0; i < path.size(); i++) {
                    System.out.print(path.get(i).getName());
                    if (i < path.size() - 1)
                        System.out.print(">");
                }
                System.out.println(" Cost:" + v.minDistance);
            }
        }
    }

    private static void pressEnterKeyToContinue(){  // see function name
        System.out.println(" [Press Enter key to continue]");
        try {
            System.in.read();
        }
        catch(Exception e){
            System.err.println("Error: " + e.getMessage());
        }
    }

    // add or delete node
    private static ArrayList<Vertex> modifyNodes(ArrayList<Vertex> vertices){
        System.out.print("Do you want to modify nodes (y/n)? ");
        Scanner sc = new Scanner(System.in);
        String ans = sc.next(); // for Y or N

        if(ans.equals("Y")||ans.equals("y")){
            System.out.print("To add or to delete(add/del)? ");
            sc = new Scanner(System.in);
            ans = sc.next();

            if(ans.equals("ADD")||ans.equals("add")){
                vertices=addNode(vertices);
            }
            else if(ans.equals("DEL")||ans.equals("del")){  // delete node
                vertices=delNode(vertices);
            }

        }else if(ans.equals("N")||ans.equals("n")){

        }
        Collections.sort(vertices, new ComparatorByName());  // sort nodeName

        return vertices;
    }

    private static ArrayList<Vertex> addNode(ArrayList<Vertex> vertices){
        System.out.println("Please type the new node relation (newNode: existNode1:cost existNode2:cost...):");
        Scanner sc = new Scanner(System.in);
        String newRecords = sc.nextLine(); // for new input records
        String[] aLineOfInput = newRecords.split("\\s+"); // split by multiple space
        try {
            if(aLineOfInput.length==1)throw new ArrayIndexOutOfBoundsException("Invalid input");
            // create neighbors array
            ArrayList<Edge> tmpNB = new ArrayList<Edge>();
            // new node
            vertices.add( new Vertex(aLineOfInput[0].charAt(0)) );
            // add neighbors

            for (int neighbors = 1; neighbors < aLineOfInput.length; neighbors++) {
                int nb = -1; // used to locate neighbor index
                System.out.println(aLineOfInput[neighbors]);
                for (Vertex tmpNode : vertices) { // search for the index of existing node
                    if (tmpNode.getName0() == (aLineOfInput[neighbors].charAt(0))) {
                        nb = vertices.indexOf(tmpNode); // index of the existing node found, add neighbors to the newNode
                    }
                }
                int cost = Integer.parseInt(aLineOfInput[neighbors].substring(2));
                tmpNB.add(new Edge(vertices.get(nb), cost)); // add adjacency to new node
                vertices.get(nb).adjacency.add(new Edge(vertices.get(vertices.size() - 1), cost)); // add adjacency to new node
            }
            vertices.get(vertices.size() - 1).adjacency = tmpNB; // add neighbor relations into newNode adjacency
        }catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
        return vertices;
    }

    private static ArrayList<Vertex> delNode(ArrayList<Vertex> vertices) {
        System.out.println("Please type one node to delete: ");
        System.out.println(vertices);    // print all existing nodes
        Scanner sc = new Scanner(System.in);
        char delNode = sc.next().charAt(0); // get the node to be removed
        int i=0;
        try {
            while(i<vertices.size()){ // loop all vertexes to search
                for(int j = 0; j<vertices.get(i).adjacency.size(); j++){
                    if(vertices.get(i).adjacency.get(j).getTargetName0()==(delNode)){  // if itself is the adjacency of other nodes, then remove
                        vertices.get(i).adjacency.remove(j);
                    }
                }
                if( vertices.get(i).getName0()==(delNode) ){ // the node to be removed found
                    vertices.remove(i);
                }else{
                    i=i+1;
                }
            }
        }catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
        return vertices;
    }

    // delete edge
    private static ArrayList<Vertex> delEdge(ArrayList<Vertex> vertices) {
        System.out.print("Do you want to delete edges (y/n)? ");
        Scanner sc = new Scanner(System.in);
        String ans = sc.next(); // for Y or N

        if(ans.equals("Y")||ans.equals("y")) {
            System.out.println("Please type the edge to delete :");
            sc = new Scanner(System.in);
            String newRecords = sc.nextLine(); // for new input records
            String[] aLineOfInput = newRecords.split("\\s+"); // split by multiple space
            char node1 = '0';
            char node2 = '0';

            if (aLineOfInput[0].length() == 1 && aLineOfInput[1].length() == 1) {
                node1 = aLineOfInput[0].charAt(0);
                node2 = aLineOfInput[1].charAt(0);
            } else if (aLineOfInput[0].length() == 2) {
                node1 = aLineOfInput[0].charAt(0);
                node2 = aLineOfInput[0].charAt(1);
            }
            try {
                int idx1 = 0;
                int idx2 = 0;
                int idx0 = 0;
                while(idx0<vertices.size()){
                    if (vertices.get(idx0).getName0()==node1)idx1=idx0;
                    if (vertices.get(idx0).getName0()==node2)idx2=idx0;
                    idx0 += 1;
                }
                for(int i = 0; i<vertices.get(idx1).adjacency.size(); i++){
                    if(vertices.get(idx1).adjacency.get(i).getTargetName0()==(node2)){
                        vertices.get(idx1).adjacency.remove(i);
                    }
                }
                for(int j = 0; j<vertices.get(idx2).adjacency.size(); j++){
                    if(vertices.get(idx2).adjacency.get(j).getTargetName0()==(node1)){
                        vertices.get(idx2).adjacency.remove(j);
                    }
                }
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
                System.exit(1);
            }
        }
        return vertices;
    }

    public static void displayAllRelations(ArrayList<Vertex> vertices){
        System.out.println("\n======= Below are the relations =======");
        for(int i=0; i<vertices.size(); i++){
            System.out.print(vertices.get(i).getName() + ": ");
            for(int j = 0; j<vertices.get(i).adjacency.size(); j++){
                System.out.print(vertices.get(i).adjacency.get(j).getTargetName() + ":" + vertices.get(i).adjacency.get(j).getCost() + " ");
            }
            System.out.println("");
        }
    }

    public static void exitOrNot(){
        System.out.println("Continue or not?(enter q to exit)");
        Scanner sc = new Scanner(System.in);
        String ans = sc.next();
        if(ans.equals("Q")||ans.equals("q")) {
            System.exit(0);
        }
    }
}
