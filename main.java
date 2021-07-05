import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;

public class main {
    private static ArrayList<String> facts = new ArrayList<String>();// Will hold the key to all facts that are true
    private static ArrayList<String> learnedVar = new ArrayList<String>();// Will hold the key to all learned variables
    private static ArrayList<String> roots = new ArrayList<String>(); // hold key to all roots
    private static ArrayList<String> rules = new ArrayList<String>();

    // Hold the key name and description
    private static HashMap<String, String> variables = new HashMap<String, String>();
    private static HashMap<String, String> ruleset = new HashMap<String, String>();

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);

        while (input.hasNextLine()) {
            String str = input.nextLine();
            if (str.equals("0"))
                break;
            str = str.replaceFirst("\\s++$", "");
            if (str.substring(str.length() - 1).equals("\"")) {
                String[] descrip = str.split(" \"");
                String[] temp = descrip[0].split(" ");
                if (temp[0].equals("Teach")) {
                    String desc = descrip[1].substring(0, descrip[1].length() - 1);
                    if (variables.putIfAbsent(temp[2], desc) == null) { // checks if it is in memory
                        if (temp[1].equals("-R")) { // checks the flag
                            roots.add(temp[2]); // adds to the correct list
                        } else {
                            learnedVar.add(temp[2]);
                        }
                    }
                }
            }
            String[] temp = str.split(" ");
            if (temp.length == 4) { // can be setting to true/false or giving a rule
                if (temp[0].equals("Teach")) {
                    if (temp[2].equals("=")) {
                        if (temp[3].equals("true") && !facts.contains(temp[1])) {
                            if (roots.contains(temp[1])) {
                                if (variables.containsKey(temp[1]))
                                    facts.add(temp[1]);
                            }
                        } else if (temp[3].equals("false") && facts.contains(temp[1]))
                            facts.remove(temp[1]);
                            removeLearn();
                    }

                    if (temp[2].equals("->")) {
                        if (ruleset.putIfAbsent(temp[1], temp[3]) == null) {
                            rules.add(temp[1]);
                        }
                    }
                }
            }

            else if (temp.length == 2) {// can be query or why
                if (temp[0].equals("Query")) {
                    Query(temp[1]);
                } else if (temp[0].equals("Why")) {
                    Why(temp[1]);
                }
            }

            else if (temp.length == 1) { // is list or learn
                if (temp[0].equals("List")) {
                    list();
                }
                if (temp[0].equals("Learn")) {
                    Learn();
                }
            }

        }
    }

    // LIST FUNCTION
    public static void list() {
        System.out.println("Root Variables:");
        for (int i = 0; i < roots.size(); i++) {
            String tempN = roots.get(i);
            System.out.println("\t" + tempN + " = \"" + variables.get(tempN) + "\" \n");
        }

        System.out.println("Learned Variables:");
        for (int i = 0; i < learnedVar.size(); i++) {
            String tempN = learnedVar.get(i);
            System.out.println("\t" + tempN + " = \"" + variables.get(tempN) + "\" \n");
        }

        System.out.println("Facts:");
        for (int i = 0; i < facts.size(); i++) {
            String tempN = facts.get(i);
            System.out.println("\t" + tempN);
        }

        System.out.println("Rules:");
        for (int i = 0; i < rules.size(); i++) {
            String tempN = rules.get(i);
            System.out.println("\t" + tempN + " -> " + ruleset.get(tempN) + " \n");
        }
    }

    // LEARN FUNCTION
    public static void Learn() {
        int change = 1;
        while (change > 0) {
            change = 0;
            ArrayList<String> trueRules = new ArrayList<String>();
            for (int j = 0; j < rules.size(); j++) {
                String str = rules.get(j);
                ArrayList<String> mem = tokenize(str);
                if (eval(mem)) {
                    trueRules.add(ruleset.get(str));
                    if (!facts.contains(ruleset.get(str))) {
                        facts.add(ruleset.get(str));
                        change++;
                    }
                }
            }
            for (int i = 0; i < rules.size(); i++) {
                String str = rules.get(i);
                ArrayList<String> mem = tokenize(str);
                if (!eval(mem) && !trueRules.contains(ruleset.get(str)) && facts.contains(ruleset.get(str))) {
                    change++;
                    facts.remove(ruleset.get(str));
                    if (roots.contains(str)) {
                        removeLearn();
                    }
                }
            }
        }
    }

    public static void removeLearn() {
        for (int i = 0; i < rules.size(); i++) {
            String str = rules.get(i);
            if (learnedVar.contains(ruleset.get(str))) {
                facts.remove(ruleset.get(str));
            }
        }
    }

    public static boolean eval(List<String> mem) {
        if (mem.size() == 1) {
            return checkFact(mem.get(0));
        }
        int lastOp = -1;
        int[] ph = new int[mem.size()];
        for (int i = 0; i < ph.length; i++) {
            ph[i] = -1;
        }
        for (int i = 0; i < mem.size(); i++) {
            String temp = mem.get(i);
            if (temp.equals("(")) {
                Stack<String> st = new Stack<String>();
                st.push("(");
                int num = i + 1;
                while (!st.isEmpty() && num < ph.length) {
                    if (mem.get(num).equals("("))
                        st.push("(");
                    else if (mem.get(num).equals(")"))
                        st.pop();
                    num++;
                }
                List<String> rec = mem.subList(i + 1, num - 1);
                int temper;
                if (eval(rec)) {
                    temper = 1; // true
                    ph[i] = 1;
                    ph[num - 1] = 1;
                } else {
                    temper = 0;
                    ph[i] = 0;
                    ph[num - 1] = 0;
                }
                for (int j = i; j < num; j++) {
                    ph[j] = temper;
                }
                lastOp = temper;
                i = num;
            }
        }
        for (int i = 0; i < mem.size(); i++) {
            String temp = mem.get(i);
            if (temp.equals("!") && ph[i] == -1) {
                if (mem.get(i + 1).equals("(")) {
                    Stack<String> st = new Stack<String>();
                    st.push("(");
                    int num = i + 2;
                    while (!st.isEmpty() && num < ph.length) {
                        if (mem.get(num).equals("("))
                            st.push("(");
                        else if (mem.get(num).equals(")"))
                            st.pop();
                        num++;
                    }
                    int tempB = ph[i + 1];
                    if (tempB == 0) {
                        tempB = 1;
                    } else {
                        tempB = 0;
                    }
                    for (int j = i; j < num; j++) {
                        ph[j] = tempB;
                    }
                    lastOp = tempB;
                    i = num;
                } else {
                    if (ph[i + 1] == -1) {
                        if (checkFact(mem.get(i + 1))) {
                            ph[i] = 0;
                            ph[i + 1] = 0; // A is true so !A
                            lastOp = 0;
                        } else {
                            ph[i] = 1;
                            ph[i + 1] = 1; // !A is true so A
                            lastOp = 1;
                        }
                    } else if (ph[i + 1] == 1) {
                        ph[i] = 0;
                        ph[i + 1] = 0; // A is true so !A
                        lastOp = 0;
                    } else {
                        ph[i] = 1;
                        ph[i + 1] = 1; // !A is true so A
                        lastOp = 1;
                    }
                }
            }
        }
        for (int i = 0; i < mem.size(); i++) {
            String temp = mem.get(i);
            if (temp.equals("&") && ph[i] == -1) {
                if (ph[i - 1] == -1) {
                    if (ph[i + 1] == -1) {
                        if (checkFact(mem.get(i - 1)) && checkFact(mem.get(i + 1))) {
                            ph[i - 1] = 1;
                            ph[i + 1] = 1;
                            ph[i] = 1;
                            lastOp = 1;
                        } else {
                            ph[i - 1] = 0;
                            ph[i + 1] = 0;
                            lastOp = 0;
                            ph[i] = 0;
                        }
                    } else {
                        if (checkFact(mem.get(i - 1)) && ph[i + 1] == 1) {
                            ph[i - 1] = 1;
                            ph[i + 1] = 1;
                            ph[i] = 1;
                            lastOp = 1;
                        } else {
                            ph[i - 1] = 0;
                            ph[i + 1] = 0;
                            lastOp = 0;
                            ph[i] = 0;
                        }
                    }
                } else if (ph[i - 1] == 1) {
                    if (ph[i + 1] == -1) {
                        if (checkFact(mem.get(i + 1))) {
                            ph[i - 1] = 1;
                            ph[i + 1] = 1;
                            ph[i] = 1;
                            lastOp = 1;
                        } else {
                            ph[i - 1] = 0;
                            ph[i + 1] = 0;
                            ph[i] = 0;
                            lastOp = 0;
                        }
                    }
                    else if (ph[i + 1] == 1) {
                        ph[i - 1] = 1;
                        ph[i + 1] = 1;
                        ph[i] = 1;
                        lastOp = 1;
                    } else {
                        ph[i - 1] = 0;
                        ph[i] = 0;
                        ph[i + 1] = 0;
                        lastOp = 0;
                    }
                } else {
                    ph[i - 1] = 0;
                    ph[i + 1] = 0;
                    ph[i] = 0;
                    lastOp = 0;
                }
            }
        }
        for (int i = 0; i < mem.size(); i++) {
            String temp = mem.get(i);
            if (temp.equals("|") && ph[i] == -1) {
                if (ph[i - 1] == -1) {
                    if (checkFact(mem.get(i - 1))) {
                        ph[i - 1] = 1;
                        ph[i] = 1;
                        ph[i + 1] = 1;
                        lastOp = 1;
                    } else {
                        if (ph[i + 1] == -1) {
                            if (checkFact(mem.get(i + 1))) {
                                ph[i - 1] = 1;
                                ph[i + 1] = 1;
                                ph[i] = 1;
                                lastOp = 1;
                            } else {
                                ph[i - 1] = 0;
                                ph[i + 1] = 0;
                                ph[i] = 0;
                                lastOp = 0;
                            }
                        } else if (ph[i + 1] == 1) {
                            ph[i - 1] = 1;
                            ph[i + 1] = 1;
                            ph[i] = 1;
                            lastOp = 1;
                        } else {
                            ph[i - 1] = 0;
                            ph[i + 1] = 0;
                            ph[i] = 0;
                            lastOp = 0;
                        }
                    }
                } else if (ph[i - 1] == 1) {
                    ph[i - 1] = 1;
                    ph[i + 1] = 1;
                    lastOp = 1;
                    ph[i] = 1;
                } else {
                    if (ph[i + 1] == -1) {
                        if (checkFact(mem.get(i + 1))) {
                            ph[i - 1] = 1;
                            ph[i + 1] = 1;
                            ph[i] = 1;
                            lastOp = 1;
                        } else {
                            ph[i - 1] = 0;
                            ph[i + 1] = 0;
                            ph[i] = 0;
                            lastOp = 0;
                        }
                    } else if (ph[i + 1] == 1) {
                        ph[i - 1] = 1;
                        ph[i + 1] = 1;
                        ph[i] = 1;
                        lastOp = 1;
                    } else {
                        ph[i - 1] = 0;
                        ph[i + 1] = 0;
                        ph[i] = 0;
                        lastOp = 0;
                    }
                }
            }
        }
        if (lastOp == 1) {
            return true;
        } else {
            return false;
        }
    }

    // Query Function


    public static void Query(String str) {
        ArrayList<String> mem = tokenize(str);
        System.out.println(queryHelper(mem));
    }

    public static boolean queryHelper(List<String> mem) {
        if (mem.size() == 1) {
            String temp = mem.get(0);
            if (checkFact(temp)){
                return true;
            }
            else {
                for(String name : rules){
                    if(ruleset.get(name).equals(temp)){
                        List<String> tList = tokenize(name);
                        if(queryHelper(tList))
                            return true;
                    }
                }
                return false;
            }
        }
        else if (mem.get(0).equals("(")) {
            Stack<String> st = new Stack<String>();
            st.push("(");
            int count = 1;
            while (!st.isEmpty() && count < mem.size()) {
                if (mem.get(count).equals("("))
                    st.push("(");
                else if (mem.get(count).equals(")"))
                    st.pop();
                count++;
            }
            if (count == mem.size())
                return queryHelper( mem.subList(1, count - 1));

            else {
                List<String> t1 = mem.subList(1, count - 1);
                List<String> t2 = mem.subList(count + 1, mem.size());
                if (mem.get(count).equals("&")) {
                    return queryHelper(t1) && queryHelper(t2);
                } else
                    return queryHelper(t1) || queryHelper(t2);
            }
        } else if (mem.get(0).equals("!")) {
            if (mem.get(1).equals("(")) {
                Stack<String> st = new Stack<String>();
                st.push("(");
                int count = 2;
                while (!st.isEmpty() && count < mem.size()) {
                    if (mem.get(count).equals("("))
                        st.push("(");
                    else if (mem.get(count).equals(")"))
                        st.pop();
                    count++;
                }
                if(count == mem.size()){
                    return !queryHelper( mem.subList(2, count-1));
                }
                else{
                    List<String> t1 = mem.subList(2, count-1);
                    List<String> t2 = mem.subList(count + 1, mem.size());
                    if(mem.get(count).equals("&")){
                        return !queryHelper(t1) && queryHelper(t2); 
                    }
                    else
                        return !queryHelper(t1) || queryHelper(t2);
                }
            }
            if(mem.size()<3){
                return !queryHelper(mem.subList(1, mem.size()));
            }
            else{
                List<String> t1 = mem.subList(1, 2);
                List<String> t2 = mem.subList(3, mem.size());
                if(mem.get(2).equals("&")){
                    return !queryHelper(t1) && queryHelper(t2);  
                }
                else
                    return !queryHelper(t1) || queryHelper(t2);
            }
        }
        else{
                List<String> t1 = mem.subList(0, 1);
                List<String> t2 = mem.subList(2, mem.size());
                if(mem.get(1).equals("&")){
                    return queryHelper(t1) && queryHelper(t2);  
                }
                else
                    return queryHelper(t1) || queryHelper(t2);
        }
    }

    // WHY Function
    public static void Why(String str) {
        ArrayList<String> strList = new ArrayList<String>();
        // Query(str);
        System.out.println(whyHelper(str, strList, false));
        for (String temp : strList) {
            if (temp.charAt(0) != '-' && temp.charAt(1) != '1')
                System.out.println(temp);
        }
    }

    public static boolean whyHelper(String str, ArrayList<String> strList, boolean parenth) {
        // List<String> mem = tokenize(str);
        // if (mem.size() == 1) {
        //     String temp = mem.get(0);
        //     if (checkFact(temp)){
        //         strList.add("I KNOW THAT " + variables.get(str));
        //         return true;
        //     }
        //     else {
        //         for(String name : rules){
        //             if(ruleset.get(name).equals(temp)){
        //                 List<String> tList = tokenize(name);
        //                 ArrayList<String> tempL = new ArrayList<String>();
        //                 if(whyHelper(name,tempL, false)){
        //                     addList(strList,tempL);
        //                     String tempStr = trim(tempL.get(tempL.size()-1));
        //                     if(!tempStr.equals(variables.get(str))){
        //                     strList.add("BECAUSE " + tempStr + " I KNOW THAT " + variables.get(str));
        //                     strList.add("-1" + variables.get(str));
        //                     }
        //                     return true;
        //                 }
        //             }
        //         }
        //         strList.add("I KNOW IT IS NOT TRUE THAT "+variables.get(str));
        //         return false;
        //     }
        // }
        // String ruleTemp = str;
        
        if (checkFact(str) && roots.contains(str)) // checks if the variable is already in Facts
        {
            strList.add("I KNOW THAT " + variables.get(str));
            return true;
        } else {
            String ruleTemp = "";
            for (String name : rules) {
                if (ruleset.get(name).equals(str)) { // checks each rule for any matches towards the desired variable
                    ruleTemp = name;
                }
            }
            if (ruleTemp.equals("")) {
                ruleTemp = str;
            }
            ArrayList<String> mem = tokenize(ruleTemp);
            // if(mem.isEmpty()){
            // return false;
            // }
            if (mem.get(0).equals(str)) {
                if (checkFact(str)) {
                    strList.add("I KNOW THAT " + variables.get(str));
                    return true;
                } else {
                    strList.add("I KNOW IT IS NOT TRUE THAT " + variables.get(str));
                    return false;
                }
            }
            //return checkFact(str);
            if (mem.get(0).equals("(")) {
                Stack<String> st = new Stack<String>();
                st.push("(");
                int count = 1;
                while (!st.isEmpty() && count < mem.size()) {
                    if (mem.get(count).equals("("))
                        st.push("(");
                    else if (mem.get(count).equals(")"))
                        st.pop();
                    count++;
                }
                String temp = listString(mem.subList(1, count - 1));
                if (count == mem.size()) {
                    if (whyHelper(temp, strList, true)) {
                        String tempStr = trim(strList.get(strList.size() - 1));
                        if (rules.contains(ruleTemp)) {
                            strList.add(
                                    "BECAUSE (" + tempStr + ") I KNOW THAT " + variables.get(ruleset.get(ruleTemp)));
                            strList.add("-1" + variables.get(ruleset.get(ruleTemp)));
                        } else {
                            // strList.add("I THUS KNOW THAT (" + tempStr + ")"); // THIS AND LINE 470 MIGHT
                            // NEED TO BE
                            // REMOVED
                            strList.add("-1(" + tempStr + ")");
                        }
                        return true;
                    } else {
                        String tempStr = trim(strList.get(strList.size() - 1));
                        if (rules.contains(ruleTemp)) {
                            strList.add("BECAUSE IT IS NOT TRUE THAT (" + tempStr + ") I CANNOT PROVE "
                                    + trim(variables.get(ruleset.get(ruleTemp))));
                            strList.add("-1" + variables.get(ruleset.get(ruleTemp)));
                        } else {
                            // strList.add("THUS I CANNOT PROVE (" + tempStr + ")");
                            strList.add("-1(" + tempStr + ")");
                        }
                        return false;
                    }
                } else {
                    String temp2 = listString(mem.subList(count + 1, mem.size()));
                    if (mem.get(count).equals("&")) {
                        ArrayList<String> tList = new ArrayList<String>();
                        if (whyHelper(temp, tList, true)) {
                            String tempStr = trim(tList.get(tList.size() - 1));
                            ArrayList<String> t2List = new ArrayList<String>();
                            if (whyHelper(temp2, t2List, false)) { // A&B : A is true, B is true
                                addList(strList, tList);
                                addList(strList, t2List);
                                String tempStr2 = trim(t2List.get(t2List.size() - 1));
                                strList.add("I THUS KNOW THAT (" + tempStr + ") AND " + tempStr2);
                                if (rules.contains(ruleTemp)) {
                                    strList.add("BECAUSE (" + tempStr + ") AND " + tempStr2 + " I KNOW THAT "
                                            + variables.get(ruleset.get(ruleTemp)));
                                    strList.add("-1" + trim(variables.get(ruleset.get(ruleTemp))));
                                } else {
                                    strList.add("-1(" + tempStr + ") AND " + tempStr2);
                                }
                                return true;
                            } else {// b is false
                                addList(strList, t2List);
                                String tempStr2 = trim(t2List.get(t2List.size() - 1));

                                strList.add("THUS I CANNOT PROVE (" + tempStr + ") AND " + tempStr2);
                                if (rules.contains(ruleTemp)) {
                                    strList.add("BECAUSE IT IS NOT TRUE THAT (" + tempStr + ") AND " + tempStr2
                                            + " I CANNOT PROVE " + variables.get(ruleset.get(ruleTemp)));
                                    strList.add("-1" + variables.get(ruleset.get(ruleTemp)));
                                } else {
                                    strList.add("-1(" + tempStr + ") AND " + tempStr2);
                                }
                                return false;
                            }
                        } else {
                            addList(strList, tList);
                            String tempStr = tList.get(tList.size() - 1);
                            tempStr = trim(tempStr);
                            whyHelper(temp2, tList, false);
                            String tempStr2 = trim(tList.get(tList.size() - 1));
                            strList.add("THUS I CANNOT PROVE (" + trim(tempStr) + ") AND " + tempStr2);
                            if (rules.contains(ruleTemp)) {
                                strList.add("BECAUSE IT IS NOT TRUE THAT (" + tempStr + ") AND " + tempStr2
                                        + " I CANNOT PROVE " + variables.get(ruleset.get(ruleTemp)));
                                strList.add("-1" + variables.get(ruleset.get(ruleTemp)));
                            } else {
                                strList.add("-1(" + tempStr + ") AND " + tempStr2);
                            }
                            return false;
                        }
                    } else {
                        ArrayList<String> tList = new ArrayList<String>();
                        if (whyHelper(temp, tList, true)) {// if A is true
                            addList(strList, tList);
                            String tempStr = trim(tList.get(tList.size() - 1));
                            ArrayList<String> tempList = new ArrayList<String>();
                            whyHelper(temp2, tempList, false);
                            String tempStr2 = trim(tempList.get(tempList.size() - 1));
                            strList.add("I THUS KNOW THAT (" + tempStr + ") OR " + tempStr2);
                            if (rules.contains(ruleTemp)) {
                                strList.add("BECAUSE (" + tempStr + ") OR " + tempStr2 + " I KNOW THAT "
                                        + trim(variables.get(ruleset.get(ruleTemp))));
                                strList.add("-1" + trim(variables.get(ruleset.get(ruleTemp))));
                            } else {
                                strList.add("-1(" + tempStr + ") OR " + tempStr2);
                            }
                            return true;
                        } else { // A IS FALSE
                            String tempStr = trim(tList.get(tList.size() - 1));
                            ArrayList<String> tempList2 = new ArrayList<String>();
                            if (whyHelper(temp2, tempList2, false)) { // b is true
                                addList(strList, tempList2);
                                String tempStr2 = trim(strList.get(strList.size() - 1));
                                // strList.add(temps);
                                strList.add("I THUS KNOW THAT (" + tempStr + ") OR " + tempStr2);
                                if (rules.contains(ruleTemp)) {
                                    strList.add("BECAUSE (" + tempStr + ") OR " + tempStr2 + " I KNOW THAT "
                                            + trim(variables.get(ruleset.get(ruleTemp))));
                                    strList.add("-1" + trim(variables.get(ruleset.get(ruleTemp))));
                                } else {
                                    strList.add("-1(" + tempStr + ") OR " + tempStr2);
                                }
                                return true;
                            } else {
                                addList(strList, tList);
                                addList(strList, tempList2);
                                String tempStr2 = trim(strList.get(strList.size() - 1));
                                strList.add("THUS I CANNOT PROVE (" + tempStr + ") OR " + tempStr2);
                                if (rules.contains(ruleTemp)) {
                                    strList.add("BECAUSE IT IS NOT TRUE THAT (" + tempStr + ") OR " + tempStr2
                                            + " I CANNOT PROVE " + trim(variables.get(ruleset.get(ruleTemp))));
                                    strList.add("-1" + trim(variables.get(ruleset.get(ruleTemp))));
                                } else {
                                    strList.add("-1(" + tempStr + ") OR " + tempStr2);
                                }
                                return false;
                            }
                        }
                    } // OR
                }
            }

            else if (mem.get(0).equals("!")) { // NEED TO EDIT BOTH TO GET ALL CASES IF ONE side DOES NOT MAKE IT
                if (mem.get(1).equals("(")) {
                    Stack<String> st = new Stack<String>();
                    st.push("(");
                    int count = 2;
                    while (!st.isEmpty() && count < mem.size()) {
                        if (mem.get(count).equals("("))
                            st.push("(");
                        else if (mem.get(count).equals(")"))
                            st.pop();
                        count++;
                    }
                    String temp = listString(mem.subList(1, count));
                    if (count == mem.size()) {
                        if (!whyHelper(temp, strList, true)) {
                            String tempStr = trim(strList.get(strList.size() - 1));
                            strList.add("I THUS KNOW THAT NOT " + tempStr);
                            if (rules.contains(ruleTemp)) {
                                strList.add("BECAUSE NOT " + tempStr + " I KNOW THAT "
                                        + variables.get(ruleset.get(ruleTemp)));
                                strList.add("-1" + variables.get(ruleset.get(ruleTemp)));
                            } else {
                                strList.add("-1NOT " + tempStr);
                            }
                            return true;
                        } else {
                            String tempStr = trim(strList.get(strList.size() - 1));
                            strList.add("THUS I CANNOT PROVE NOT " + tempStr);
                            if (rules.contains(ruleTemp)) {
                                strList.add("BECAUSE IT IS NOT TRUE THAT NOT " + trim(tempStr) + " I CANNOT PROVE "
                                        + trim(variables.get(ruleset.get(ruleTemp))));
                                strList.add("-1" + trim(variables.get(ruleset.get(ruleTemp))));
                            } else {
                                strList.add("-1NOT " + tempStr);
                            }
                            return false;
                        }
                    } else {
                        String temp2 = listString(mem.subList(count + 1, mem.size()));
                        if (mem.get(count).equals("&")) {
                            ArrayList<String> tList = new ArrayList<String>();
                            if (!whyHelper(temp, tList, true)) {
                                String tempStr = trim(tList.get(tList.size() - 1));// trim(strList.get(strList.size()));
                                tList.add("I THUS KNOW THAT NOT " + tempStr);
                                ArrayList<String> t2List = new ArrayList<String>();
                                if (whyHelper(temp2, t2List, false)) {
                                    addList(strList, tList);
                                    addList(strList, t2List);
                                    String tempStr2 = trim(strList.get(strList.size() - 1));
                                    strList.add("I THUS KNOW THAT NOT " + tempStr + " AND " + tempStr2);
                                    if (rules.contains(ruleTemp)) {
                                        strList.add("BECAUSE NOT " + tempStr + " AND " + tempStr2 + " I KNOW THAT "
                                                + variables.get(ruleset.get(ruleTemp)));
                                        strList.add("-1" + variables.get(ruleset.get(ruleTemp)));
                                    }
                                    return true;
                                } else { // !A&B where B is false
                                    addList(strList, t2List);
                                    String tempStr2 = trim(t2List.get(t2List.size() - 1));
                                    strList.add("THUS I CANNOT PROVE NOT " + tempStr + " AND " + tempStr2);
                                    if (rules.contains(ruleTemp)) {
                                        strList.add("BECAUSE IT IS NOT TRUE THAT NOT " + tempStr + " AND " + tempStr2
                                                + " I CANNOT PROVE " + variables.get(ruleset.get(ruleTemp)));
                                        strList.add("-1" + variables.get(ruleset.get(ruleTemp)));
                                    }
                                    return false;
                                }
                            } else { // WHERE A IS FALSE
                                addList(strList, tList);
                                String tempStr = tList.get(tList.size() - 1);
                                tempStr = trim(tempStr);
                                whyHelper(temp2, tList, false);
                                String tempStr2 = trim(tList.get(tList.size() - 1));

                                strList.add("THUS I CANNOT PROVE NOT " + tempStr);
                                strList.add("THUS I CANNOT PROVE NOT " + tempStr + " AND " + tempStr2);
                                if (rules.contains(ruleTemp)) {
                                    strList.add("BECAUSE IT IS NOT TRUE THAT NOT " + tempStr + " AND " + tempStr2
                                            + " I CANNOT PROVE " + variables.get(ruleset.get(ruleTemp)));
                                    strList.add("-1" + variables.get(ruleset.get(ruleTemp)));
                                }
                                return false;
                            }
                        } else {// OR
                            ArrayList<String> tList = new ArrayList<String>();
                            if (!whyHelper(temp, tList, true)) {// if A is true
                                addList(strList, tList);
                                String tempStr = trim(strList.get(strList.size() - 1));
                                ArrayList<String> tempList = new ArrayList<String>();
                                whyHelper(temp2, tempList, false); // to get total string
                                String tempStr2 = trim(trim(tempList.get(tempList.size() - 1)));
                                strList.add("I THUS KNOW THAT NOT " + tempStr);
                                strList.add("I THUS KNOW THAT NOT " + tempStr + " OR " + tempStr2);
                                if (rules.contains(ruleTemp)) {
                                    strList.add("BECAUSE NOT " + tempStr + " OR " + tempStr2 + " I KNOW THAT "
                                            + trim(variables.get(ruleset.get(ruleTemp))));
                                    strList.add("-1" + trim(variables.get(ruleset.get(ruleTemp))));
                                }
                                return true;
                            } else { // A IS FALSE
                                String tempStr = trim(tList.get(tList.size() - 1));
                                ArrayList<String> tempList2 = new ArrayList<String>();
                                if (whyHelper(temp2, tempList2, false)) { // b is true
                                    addList(strList, tempList2);
                                    String tempStr2 = trim(strList.get(strList.size() - 1));
                                    strList.add("I THUS KNOW THAT NOT " + tempStr + " OR " + tempStr2);
                                    if (rules.contains(ruleTemp)) {
                                        strList.add("BECAUSE " + tempStr + " OR " + tempStr2 + " I KNOW THAT "
                                                + trim(variables.get(ruleset.get(ruleTemp))));
                                        strList.add("-1" + trim(variables.get(ruleset.get(ruleTemp))));
                                    }
                                    return true;
                                } else {
                                    addList(strList, tList);
                                    addList(strList, tempList2);
                                    String tempStr2 = trim(strList.get(strList.size() - 1));
                                    strList.add("THUS I CANNOT PROVE NOT " + tempStr + " OR " + tempStr2);
                                    if (rules.contains(ruleTemp)) {
                                        strList.add("BECAUSE IT IS NOT TRUE THAT NOT " + tempStr + " OR " + tempStr2
                                                + " I CANNOT PROVE " + trim(variables.get(ruleset.get(ruleTemp))));
                                        strList.add("-1" + trim(variables.get(ruleset.get(ruleTemp))));
                                    }
                                    return false;
                                }
                            }
                        }
                    }
                }
                if (mem.size() < 3) {
                    if (!whyHelper(mem.get(1), strList, false)) {
                        String tempStr = trim(strList.get(strList.size() - 1));
                        if (parenth) {
                            strList.add("I THUS KNOW THAT NOT (" + tempStr + ")");
                        } else {
                            strList.add("I THUS KNOW THAT NOT " + tempStr);
                        }
                        if (rules.contains(ruleTemp)) {
                            strList.add(
                                    "BECAUSE NOT " + tempStr + " I KNOW THAT " + variables.get(ruleset.get(ruleTemp)));
                            strList.add("-1" + variables.get(ruleset.get(ruleTemp)));
                        } else {
                            strList.add("-1NOT " + tempStr);
                        }
                        return true;
                    } else {
                        String tempStr = trim(strList.get(strList.size() - 1));
                        if (parenth) {
                            strList.add("THUS I CANNOT PROVE NOT (" + tempStr + ")");
                        } else {
                            strList.add("THUS I CANNOT PROVE NOT " + tempStr);
                        }
                        if (rules.contains(ruleTemp)) {
                            strList.add("BECAUSE IT IS NOT TRUE THAT NOT " + trim(tempStr) + " I CANNOT PROVE "
                                    + trim(variables.get(ruleset.get(ruleTemp))));
                            strList.add("-1" + trim(variables.get(ruleset.get(ruleTemp))));
                        } else {
                            strList.add("-1NOT " + tempStr);
                        }
                        return false;
                    }
                } else { // NEED TO EDIT BOTH TO GET ALL CASES IF ONE side DOES NOT MAKE IT
                    String temp = listString(mem.subList(3, mem.size()));
                    if (mem.get(2).equals("&")) {
                        ArrayList<String> tList = new ArrayList<String>();
                        if (!whyHelper(mem.get(1), tList, false)) {
                            String tempStr = trim(tList.get(tList.size() - 1));// trim(strList.get(strList.size()));
                            tList.add("I THUS KNOW THAT NOT " + tempStr);
                            ArrayList<String> t2List = new ArrayList<String>();
                            if (whyHelper(temp, t2List, false)) {
                                addList(strList, tList);
                                addList(strList, t2List);
                                String tempStr2 = trim(strList.get(strList.size() - 1));
                                if (parenth) {
                                    strList.add("I THUS KNOW THAT NOT (" + tempStr + " AND " + tempStr2 + ")");
                                } else {
                                    strList.add("I THUS KNOW THAT NOT " + tempStr + " AND " + tempStr2);
                                }
                                if (rules.contains(ruleTemp)) {
                                    strList.add("BECAUSE NOT " + tempStr + " AND " + tempStr2 + " I KNOW THAT "
                                            + variables.get(ruleset.get(ruleTemp)));
                                    strList.add("-1" + variables.get(ruleset.get(ruleTemp)));
                                } else {
                                    strList.add("-1NOT " + tempStr + " AND " + tempStr2);
                                }
                                return true;
                            } else { // !A&B where B is false
                                addList(strList, t2List);
                                String tempStr2 = trim(t2List.get(t2List.size() - 1));
                                if (parenth) {
                                    strList.add("THUS I CANNOT PROVE NOT (" + tempStr + " AND " + tempStr2 + ")");
                                } else {
                                    strList.add("THUS I CANNOT PROVE NOT " + tempStr + " AND " + tempStr2);
                                }
                                if (rules.contains(ruleTemp)) {
                                    strList.add("BECAUSE IT IS NOT TRUE THAT NOT " + tempStr + " AND "
                                            + trim(strList.get(strList.size() - 1)) + " I CANNOT PROVE "
                                            + variables.get(ruleset.get(ruleTemp)));
                                    strList.add("-1" + variables.get(ruleset.get(ruleTemp)));
                                } else {
                                    strList.add("-1NOT " + tempStr + " AND " + tempStr2);
                                }
                                return false;
                            }
                        } else { // WHERE A IS FALSE
                            addList(strList, tList);
                            String tempStr = tList.get(tList.size() - 1);
                            tempStr = trim(tempStr);
                            whyHelper(temp, tList, false);
                            String tempStr2 = trim(tList.get(tList.size() - 1));
                            strList.add("THUS I CANNOT PROVE NOT " + tempStr);
                            if (parenth) {
                                strList.add("THUS I CANNOT PROVE NOT (" + tempStr + " AND " + tempStr2 + ")");
                            } else {
                                strList.add("THUS I CANNOT PROVE NOT " + tempStr + " AND " + tempStr2);
                            }
                            if (rules.contains(ruleTemp)) {
                                strList.add("BECAUSE IT IS NOT TRUE THAT NOT " + tempStr + " AND " + tempStr2
                                        + " I CANNOT PROVE " + variables.get(ruleset.get(ruleTemp)));
                                strList.add("-1" + variables.get(ruleset.get(ruleTemp)));
                            } else {
                                strList.add("-1NOT " + tempStr + " AND " + tempStr2);
                            }
                            return false;
                        }
                    } else { // OR
                        ArrayList<String> tList = new ArrayList<String>();
                        if (!whyHelper(mem.get(1), tList, false)) {// if A is true
                            addList(strList, tList);
                            String tempStr = trim(strList.get(strList.size() - 1));
                            ArrayList<String> tempList = new ArrayList<String>();
                            whyHelper(temp, tempList, false); // to get total string
                            String tempStr2 = trim(tempList.get(tempList.size() - 1));
                            strList.add("I THUS KNOW THAT NOT " + tempStr);
                            if (parenth) {
                                strList.add("I THUS KNOW THAT NOT (" + tempStr + " OR " + tempStr2 + ")");
                            } else {
                                strList.add("I THUS KNOW THAT NOT " + tempStr + " OR " + tempStr2);
                            }
                            if (rules.contains(ruleTemp)) {
                                strList.add("BECAUSE NOT " + tempStr + " OR " + tempStr2 + " I KNOW THAT "
                                        + trim(variables.get(ruleset.get(ruleTemp))));
                                strList.add("-1" + trim(variables.get(ruleset.get(ruleTemp))));
                            } else {
                                strList.add("-1NOT " + tempStr + " OR " + tempStr2);
                            }
                            return true;
                        } else { // A IS FALSE
                            String tempStr = trim(tList.get(tList.size() - 1));
                            ArrayList<String> tempList2 = new ArrayList<String>();
                            if (whyHelper(temp, tempList2, false)) { // b is true
                                addList(strList, tempList2);
                                String tempStr2 = trim(strList.get(strList.size() - 1));
                                // strList.add(temps);
                                if (parenth) {
                                    strList.add("I THUS KNOW THAT NOT (" + tempStr + " OR " + tempStr2 + ")");
                                } else {
                                    strList.add("I THUS KNOW THAT NOT " + tempStr + " OR " + tempStr2);
                                }
                                if (rules.contains(ruleTemp)) {
                                    strList.add("BECAUSE " + tempStr + " OR " + tempStr2 + " I KNOW THAT "
                                            + trim(variables.get(ruleset.get(ruleTemp))));
                                    strList.add("-1" + trim(variables.get(ruleset.get(ruleTemp))));
                                } else {
                                    strList.add("-1NOT " + tempStr + " OR " + tempStr2);
                                }
                                return true;
                            } else {
                                addList(strList, tList);
                                addList(strList, tempList2);
                                String tempStr2 = trim(strList.get(strList.size() - 1));
                                if (parenth) {
                                    strList.add("THUS I CANNOT PROVE NOT (" + tempStr + " OR " + tempStr2 + ")");
                                } else {
                                    strList.add("THUS I CANNOT PROVE NOT " + tempStr + " OR " + tempStr2);
                                }
                                if (rules.contains(ruleTemp)) {
                                    strList.add("BECAUSE IT IS NOT TRUE THAT NOT " + tempStr + " OR " + tempStr2
                                            + " I CANNOT PROVE " + trim(variables.get(ruleset.get(ruleTemp))));
                                    strList.add("-1" + trim(variables.get(ruleset.get(ruleTemp))));
                                } else {
                                    strList.add("-1NOT " + tempStr + " OR " + tempStr2);
                                }
                                return false;
                            }
                        }
                    }
                }
            } else { // standard
                if (mem.size() < 2) {
                    if (whyHelper(mem.get(0), strList, false)) {
                        String tempStr = trim(strList.get(strList.size() - 1));
                        if (rules.contains(ruleTemp)) {
                            strList.add("BECAUSE " + tempStr + " I KNOW THAT " + variables.get(ruleset.get(ruleTemp)));
                            strList.add("-1" + variables.get(ruleset.get(ruleTemp)));
                        } else {
                            if (parenth) {
                                strList.add("I THUS KNOW THAT (" + tempStr + ")");
                            } else {
                                strList.add("I THUS KNOW THAT " + tempStr);
                            }
                            strList.add("-1" + tempStr);
                        }
                        return true;
                    } else {
                        String tempStr = trim(strList.get(strList.size() - 1));
                        if (rules.contains(ruleTemp)) {
                            strList.add("BECAUSE IT IS NOT TRUE THAT " + trim(tempStr) + " I CANNOT PROVE "
                                    + variables.get(ruleset.get(ruleTemp)));
                            strList.add("-1" + variables.get(ruleset.get(ruleTemp)));
                        } else {
                            if (parenth) {
                                strList.add("THUS I CANNOT PROVE (" + tempStr + ")");
                            } else {
                                strList.add("THUS I CANNOT PROVE " + tempStr);
                            }
                            strList.add("-1" + tempStr);
                        }
                        return false;
                    }
                } else { // NEED TO EDIT BOTH TO GET ALL CASES IF ONE side DOES NOT MAKE IT
                    String temp = listString(mem.subList(2, mem.size()));
                    if (mem.get(1).equals("&")) {
                        ArrayList<String> tList = new ArrayList<String>();
                        if (whyHelper(mem.get(0), tList, false)) {
                            String tempStr = trim(tList.get(tList.size() - 1));
                            ArrayList<String> t2List = new ArrayList<String>();
                            if (whyHelper(temp, t2List, false)) { // A&B : A is true, B is true
                                addList(strList, tList);
                                addList(strList, t2List);
                                String tempStr2 = trim(t2List.get(t2List.size() - 1));
                                if (parenth) {
                                    strList.add("I THUS KNOW THAT (" + tempStr + " AND " + tempStr2 + ")");
                                } else {
                                    strList.add("I THUS KNOW THAT " + tempStr + " AND " + tempStr2);
                                }
                                if (rules.contains(ruleTemp)) {
                                    strList.add("BECAUSE " + tempStr + " AND " + tempStr2 + " I KNOW THAT "
                                            + trim(variables.get(ruleset.get(ruleTemp))));
                                    strList.add("-1" + trim(variables.get(ruleset.get(ruleTemp))));
                                } else {
                                    strList.add("-1" + tempStr + " AND " + tempStr2);
                                }
                                return true;
                            } else {// b is false
                                addList(strList, t2List);
                                String tempStr2 = trim(t2List.get(t2List.size() - 1));
                                if (parenth) {
                                    strList.add("THUS I CANNOT PROVE (" + tempStr + " AND " + tempStr2 + ")");
                                } else {
                                    strList.add("THUS I CANNOT PROVE " + tempStr + " AND " + tempStr2);
                                }
                                if (rules.contains(ruleTemp)) {
                                    strList.add("BECAUSE IT IS NOT TRUE THAT " + tempStr + " AND " + tempStr2
                                            + " I CANNOT PROVE " + variables.get(ruleset.get(ruleTemp)));
                                    strList.add("-1" + variables.get(ruleset.get(ruleTemp)));
                                } else {
                                    strList.add("-1" + tempStr + " AND " + tempStr2);
                                }
                                return false;
                            }
                        } else {
                            addList(strList, tList);
                            String tempStr = tList.get(tList.size() - 1);
                            tempStr = trim(tempStr);
                            whyHelper(temp, tList, false);
                            String tempStr2 = trim(tList.get(tList.size() - 1));
                            if (parenth) {
                                strList.add("THUS I CANNOT PROVE (" + trim(tempStr) + " AND " + tempStr2 + ")");
                            } else {
                                strList.add("THUS I CANNOT PROVE " + trim(tempStr) + " AND " + tempStr2);
                            }
                            if (rules.contains(ruleTemp)) {
                                strList.add("BECAUSE IT IS NOT TRUE THAT " + tempStr + " AND " + tempStr2
                                        + " I CANNOT PROVE " + variables.get(ruleset.get(ruleTemp)));
                                strList.add("-1" + variables.get(ruleset.get(ruleTemp)));
                            } else {
                                strList.add("-1" + tempStr + " AND " + tempStr2);
                            }
                            return false;
                        }
                    } else { // else if (mem.get(1).equals("|")) .... OR
                        ArrayList<String> tList = new ArrayList<String>();
                        if (whyHelper(mem.get(0), tList, false)) {// if A is true
                            addList(strList, tList);
                            String tempStr = trim(tList.get(tList.size() - 1));
                            ArrayList<String> tempList = new ArrayList<String>();
                            whyHelper(temp, tempList, false);
                            String tempStr2 = trim(tempList.get(tempList.size() - 1));
                            if (parenth) {
                                strList.add("I THUS KNOW THAT (" + tempStr + " OR " + tempStr2 + ")");
                            } else {
                                strList.add("I THUS KNOW THAT " + tempStr + " OR " + tempStr2);
                            }
                            if (rules.contains(ruleTemp)) {
                                strList.add("BECAUSE " + tempStr + " OR " + tempStr2 + " I KNOW THAT "
                                        + trim(variables.get(ruleset.get(ruleTemp))));
                                strList.add("-1" + trim(variables.get(ruleset.get(ruleTemp))));
                            } else {
                                strList.add("-1" + tempStr + " OR " + tempStr2);
                            }
                            return true;
                        } else { // A IS FALSE
                            String tempStr = trim(tList.get(tList.size() - 1));
                            ArrayList<String> tempList2 = new ArrayList<String>();
                            if (whyHelper(temp, tempList2, false)) { // b is true
                                addList(strList, tempList2);
                                String tempStr2 = trim(strList.get(strList.size() - 1));
                                if (parenth) {
                                    strList.add("I THUS KNOW THAT (" + tempStr + " OR " + tempStr2 + ")");
                                } else {
                                    strList.add("I THUS KNOW THAT " + tempStr + " OR " + tempStr2);
                                }
                                if (rules.contains(ruleTemp)) {
                                    strList.add("BECAUSE " + tempStr + " OR " + tempStr2 + " I KNOW THAT "
                                            + trim(variables.get(ruleset.get(ruleTemp))));
                                    strList.add("-1" + trim(variables.get(ruleset.get(ruleTemp))));
                                } else {
                                    strList.add("-1" + tempStr + " OR " + tempStr2);
                                }
                                return true;
                            } else {
                                addList(strList, tList);
                                addList(strList, tempList2);
                                String tempStr2 = trim(strList.get(strList.size() - 1));
                                if (parenth) {
                                    strList.add("THUS I CANNOT PROVE (" + tempStr + " OR " + tempStr2 + ")");
                                } else {
                                    strList.add("THUS I CANNOT PROVE " + tempStr + " OR " + tempStr2);
                                }
                                if (rules.contains(ruleTemp)) {
                                    strList.add("BECAUSE IT IS NOT TRUE THAT " + tempStr + " OR " + tempStr2
                                            + " I CANNOT PROVE " + trim(variables.get(ruleset.get(ruleTemp))));
                                    strList.add("-1" + trim(variables.get(ruleset.get(ruleTemp))));
                                } else {
                                    strList.add("-1" + tempStr + " OR " + tempStr2);
                                }
                                return false;
                            }
                        }
                    }
                }
            }
        }
    } // take this one out when fixing


    public static void addList(List<String> a, List<String> b) {
        for (String x : b) {
            a.add(x);
        }
    }

    public static String trim(String str) {
        String NIINTT = "BECAUSE IT IS NOT TRUE THAT "; // 28
        String TICP = "THUS I CANNOT PROVE ";
        String ikiintt = "I KNOW IT IS NOT TRUE THAT "; // 27
        String ITKT = "I THUS KNOW THAT "; // 17
        String IKT = "I KNOW THAT "; // 12
        String b = "BECAUSE ";
        String flag = "-1";
        String temp = str;
        if (str.length() > 2) {
            if (str.substring(0, 2).equals(flag))
                temp = str.substring(2);
        }
        if (str.length() > 8) {
            if (str.substring(0, 8).equals(b))
                temp = str.substring(8);
        }
        if (str.length() > 12) {
            if (str.substring(0, 12).equals(IKT))
                temp = str.substring(12);
        }
        if (str.length() > 17) {
            if (str.substring(0, 17).equals(ITKT))
                temp = str.substring(17);
        }
        if (str.length() > 20) {
            if (str.substring(0, 20).equals(TICP))
                temp = str.substring(20);
        }
        if (str.length() > 27) {
            if (str.substring(0, 27).equals(ikiintt))
                temp = str.substring(27);
        }
        if (str.length() > 28) {
            if (str.substring(0, 28).equals(NIINTT))
                temp = str.substring(28);
        }
        return temp;
    }

    public static String listString(List<String> mem) {
        String temp = "";
        for (String str : mem) {
            temp += str;
        }
        return temp;
    }

    public static boolean checkFact(String str) {
        if (facts.contains(str)) {
            return true;
        } else
            return false;
    }

    public static ArrayList<String> tokenize(String str) {
        ArrayList<String> mem = new ArrayList<String>(); // tokenize everything
        String temp = "";
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == '(') {
                if (temp != "") {
                    mem.add(temp);
                    temp = "";
                }
                mem.add("(");
            } else if (str.charAt(i) == ')') {
                if (temp != "") {
                    mem.add(temp);
                    temp = "";
                }
                mem.add(")");
            } else if (str.charAt(i) == '&') {
                if (temp != "") {
                    mem.add(temp);
                    temp = "";
                }
                mem.add("&");
            } else if (str.charAt(i) == '|') {
                if (temp != "") {
                    mem.add(temp);
                    temp = "";
                }
                mem.add("|");
            } else if (str.charAt(i) == '!') {
                if (temp != "") {
                    mem.add(temp);
                    temp = "";
                }
                mem.add("!");
            } else {
                temp += str.charAt(i);
            }
        }
        mem.add(temp);
        mem.remove("");
        return mem;
    }
}