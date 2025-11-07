import java.io.*;
import java.util.*;
import java.util.regex.*;

public class Pass2Assembler {

    static class Symbol {
        String name;
        int address;
        Symbol(String n, int a) { name = n; address = a; }
    }

    static class Literal {
        String literal;
        int address;
        Literal(String l, int a) { literal = l; address = a; }
    }

    static Map<Integer, Symbol> symtab = new HashMap<>();
    static Map<Integer, Literal> littab = new HashMap<>();

    public static void main(String[] args) throws Exception {
        // Default filenames for simplicity
        String intermediateFile = "intermediate.txt";
        String symFile = "symtab.txt";
        String litFile = "littab.txt";
        String poolFile = "pooltab.txt"; // not used but kept for structure

        // Load tables
        loadSymtab(symFile);
        loadLittab(litFile);

        // Generate machine code
        generateMachineCode(intermediateFile);

        System.out.println("PASS 2 completed ✅");
        System.out.println("Output generated: machine.txt");
    }

    static void loadSymtab(String file) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("Index")) continue;
                String[] parts = line.split("\\s+");
                if (parts.length >= 3) {
                    try {
                        int index = Integer.parseInt(parts[0]);
                        String name = parts[1];
                        int addr = Integer.parseInt(parts[2]);
                        symtab.put(index - 1, new Symbol(name, addr)); // match (S,index) in IC
                    } catch (Exception ignored) {}
                }
            }
        }
    }

    static void loadLittab(String file) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("Index")) continue;
                String[] parts = line.split("\\s+");
                if (parts.length >= 3) {
                    try {
                        int index = Integer.parseInt(parts[0]);
                        String literal = parts[1];
                        int addr = Integer.parseInt(parts[2]);
                        littab.put(index - 1, new Literal(literal, addr)); // match (L,index)
                    } catch (Exception ignored) {}
                }
            }
        }
    }

    static void generateMachineCode(String file) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(file));
             BufferedWriter bw = new BufferedWriter(new FileWriter("machine.txt"))) {

            bw.write("ADDR\tMACHINE CODE\n");

            Pattern pattern = Pattern.compile("\\((\\w+),(\\d+)\\)");

            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] tokens = line.split("\\s+");
                String addr = tokens[0];
                StringBuilder mc = new StringBuilder();

                Matcher m = pattern.matcher(line);
                if (m.find()) {
                    String type = m.group(1);
                    String code = m.group(2);

                    // ====== IMPERATIVE STATEMENTS ======
                    if (type.equals("IS")) {
                        mc.append(code).append(" ");
                        // Extract operands (registers, symbols, literals)
                        for (String t : tokens) {
                            if (t.matches("\\d+")) {
                                mc.append(t).append(" ");
                            } else if (t.startsWith("(S,")) {
                                int idx = Integer.parseInt(t.substring(3, t.length() - 1));
                                Symbol s = symtab.get(idx);
                                mc.append((s != null) ? s.address : 0).append(" ");
                            } else if (t.startsWith("(L,")) {
                                int idx = Integer.parseInt(t.substring(3, t.length() - 1));
                                Literal l = littab.get(idx);
                                mc.append((l != null) ? l.address : 0).append(" ");
                            }
                        }
                    }

                    // ====== DECLARATIVE STATEMENTS ======
                    else if (type.equals("DL")) {
                        if (code.equals("01")) { // DC
                            mc.append("00 0 ");
                            if (tokens.length > 2) mc.append(tokens[tokens.length - 1]);
                            else mc.append("0");
                        } else if (code.equals("02")) { // DS
                            mc.append("00 0 0");
                        }
                    }
                    // Assembler Directives (AD) produce no machine code
                }

                // Only write machine code lines
                if (mc.length() > 0)
                    bw.write(addr + "\t" + mc.toString().trim() + "\n");
            }
        }
    }
}
