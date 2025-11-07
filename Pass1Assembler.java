import java.io.*;
import java.util.*;
import java.util.regex.*;

public class Pass1Assembler {

    // === OPCODE TABLES ===
    static final Map<String, Integer> IS = new LinkedHashMap<>();
    static {
        IS.put("STOP", 0); IS.put("ADD", 1); IS.put("SUB", 2); IS.put("MULT", 3);
        IS.put("MOVER", 4); IS.put("MOVEM", 5); IS.put("COMP", 6); IS.put("BC", 7);
        IS.put("DIV", 8); IS.put("READ", 9); IS.put("PRINT", 10);
    }

    static final Map<String, Integer> DL = Map.of("DC", 1, "DS", 2);
    static final Map<String, Integer> AD = Map.of("START", 1, "END", 2, "ORIGIN", 3, "EQU", 4, "LTORG", 5);
    static final Map<String, Integer> REG = Map.of(
            "AREG", 1, "BREG", 2, "CREG", 3, "DREG", 4);
    static final Map<String, Integer> COND = Map.of("LT", 1, "LE", 2, "EQ", 3, "GT", 4, "GE", 5, "ANY", 6);

    // === DATA STRUCTURES ===
    static class Sym { int i, a = -1; String n; Sym(int i, String n) { this.i = i; this.n = n; } }
    static class Lit { int i, v, a = -1; String r; Lit(int i, String r, int v) { this.i = i; this.r = r; this.v = v; } }

    List<String> IC = new ArrayList<>();
    List<Sym> ST = new ArrayList<>();
    Map<String, Integer> SIDX = new LinkedHashMap<>();
    List<Lit> LT = new ArrayList<>();
    List<Integer> PT = new ArrayList<>();

    int LC = 0, poolStart = 1;

    static final Pattern LIT = Pattern.compile("^=\\s*['\"]?([+-]?\\d+)['\"]?$");
    static final Pattern LAB = Pattern.compile("^([A-Za-z_][\\w]*)\\s*:\\s*(.*)$");

    // === SYMBOL TABLE HANDLING ===
    int sym(String n) {
        return SIDX.computeIfAbsent(n, k -> {
            int i = ST.size() + 1;
            ST.add(new Sym(i, k));
            return i;
        });
    }
    void def(String n, int addr) { ST.get(sym(n) - 1).a = addr; }
    boolean isNum(String s) { try { Integer.parseInt(s.trim()); return true; } catch (Exception e) { return false; } }
    boolean isLit(String s) { return LIT.matcher(s.trim()).matches(); }
    int litVal(String r) {
        Matcher m = LIT.matcher(r.trim());
        if (!m.matches()) throw new RuntimeException("Bad literal " + r);
        return Integer.parseInt(m.group(1));
    }
    int lit(String r) {
        int i = LT.size() + 1;
        LT.add(new Lit(i, r, litVal(r)));
        return i;
    }

    // === LITERAL POOL HANDLING ===
    void flushPool() {
        int s = poolStart, e = LT.size();
        if (s > e) return;
        PT.add(s);
        for (int i = s; i <= e; i++) {
            Lit L = LT.get(i - 1);
            if (L.a == -1) {
                L.a = LC;
                IC.add(LC + " (DL,01) " + L.v);
                LC++;
            }
        }
        poolStart = LT.size() + 1;
    }

    // === EXPRESSION EVALUATION ===
    int getAddr(String symName) {
        Integer idx = SIDX.get(symName);
        if (idx == null || ST.get(idx - 1).a == -1)
            throw new RuntimeException("Undefined symbol: " + symName);
        return ST.get(idx - 1).a;
    }

    int eval(String expr) {
        String e = expr.replaceAll("\\s+", "");
        if (isNum(e)) return Integer.parseInt(e);
        int p = Math.max(e.lastIndexOf('+'), e.lastIndexOf('-'));
        if (p > 0) {
            String sym = e.substring(0, p);
            int off = Integer.parseInt(e.substring(p));
            return getAddr(sym) + off;
        }
        return getAddr(e);
    }

    String originIC(String expr) {
        String e = expr.replaceAll("\\s+", "");
        if (isNum(e)) return e;
        int p = Math.max(e.lastIndexOf('+'), e.lastIndexOf('-'));
        if (p > 0)
            return "(S," + (sym(e.substring(0, p)) - 1) + ")" + e.substring(p);
        else
            return "(S," + (sym(e) - 1) + ")";
    }

    String[] splitOps(String f) {
        if (f == null) return new String[0];
        return Arrays.stream(f.split(","))
                .map(String::trim).filter(s -> !s.isEmpty()).toArray(String[]::new);
    }

    void appendOperand(StringBuilder b, String tok) {
        String t = tok.trim();
        if (t.isEmpty()) return;
        Integer r = REG.get(t.toUpperCase());
        if (r != null) b.append(r).append(", ");
        else if (isLit(t)) b.append("(L,").append(lit(t) - 1).append(")");
        else if (isNum(t)) b.append(t);
        else b.append("(S,").append(sym(t) - 1).append(")");
    }

    // === PROCESS EACH LINE ===
    void process(String raw) {
        String line = raw.trim();
        if (line.isEmpty()) return;

        String label = null, opcode, operand = null;
        Matcher m = LAB.matcher(line);
        if (m.matches()) {
            label = m.group(1);
            line = m.group(2).trim();
        }

        String[] parts = line.split("\\s+", 2);
        opcode = parts[0].toUpperCase();
        if (parts.length > 1) operand = parts[1];

        // define label
        if (label != null && !"EQU".equalsIgnoreCase(opcode))
            def(label, LC);

        // === ASSEMBLER DIRECTIVES ===
        if (AD.containsKey(opcode)) {
            switch (opcode) {
                case "START":
                    LC = (operand == null || operand.isEmpty()) ? 0 : Integer.parseInt(operand.trim());
                    IC.add(LC + " (AD,01)");
                    break;
                case "END":
                    IC.add(LC + " (AD,02)");
                    flushPool();
                    break;
                case "LTORG":
                    IC.add(LC + " (AD,05)");
                    flushPool();
                    break;
                case "ORIGIN":
                    IC.add(LC + " (AD,03) " + originIC(operand));
                    LC = eval(operand);
                    break;
                case "EQU":
                    if (label == null) throw new RuntimeException("EQU without label");
                    def(label, eval(operand));
                    IC.add(LC + " (AD,04) " + originIC(operand));
                    break;
            }
            return;
        }

        // === DECLARATIVE STATEMENTS ===
        if (DL.containsKey(opcode)) {
            String[] ops = splitOps(operand);
            if ("DC".equals(opcode)) {
                int val = isLit(ops[0]) ? litVal(ops[0]) : Integer.parseInt(ops[0]);
                IC.add(LC + " (DL,01) " + val);
                LC++;
            } else if ("DS".equals(opcode)) {
                int size = Integer.parseInt(ops[0]);
                IC.add(LC + " (DL,02) " + size);
                LC += size;
            }
            return;
        }

        // === INSTRUCTION STATEMENTS ===
        if (IS.containsKey(opcode)) {
            int code = IS.get(opcode);
            StringBuilder b = new StringBuilder(LC + " (IS," + String.format("%02d", code) + ") ");
            String[] ops = splitOps(operand);

            if ("STOP".equals(opcode)) {
                IC.add(b.toString().trim());
                LC++;
                return;
            }
            if ("READ".equals(opcode) || "PRINT".equals(opcode)) {
                appendOperand(b, ops[0]);
                IC.add(b.toString().trim());
                LC++;
                return;
            }
            if ("BC".equals(opcode)) {
                int cc = COND.getOrDefault(ops[0].toUpperCase(), 0);
                b.append(cc).append(", ");
                appendOperand(b, ops[1]);
                IC.add(b.toString().trim());
                LC++;
                return;
            }

            // for ADD, MOVER, MOVEM, etc.
            if (ops.length >= 2) {
                appendOperand(b, ops[0]);
                b.append(" ");
                appendOperand(b, ops[1]);
            } else if (ops.length == 1) {
                appendOperand(b, ops[0]);
            }

            IC.add(b.toString().trim());
            LC++;
            return;
        }

        throw new RuntimeException("Unknown opcode: " + opcode);
    }

    // === MAIN RUN ===
    void run(String path) throws Exception {
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String s;
            while ((s = br.readLine()) != null)
                process(s);
        }

        // if literals remain unprocessed
        flushPool();

        // === WRITE OUTPUT FILES ===
        try (PrintWriter pw = new PrintWriter("intermediate.txt")) {
            for (String x : IC)
                pw.println(x);
        }

        try (PrintWriter pw = new PrintWriter("symtab.txt")) {
            pw.println("Index\tSymbol\tAddress");
            for (Sym x : ST)
                pw.println(x.i + "\t" + x.n + "\t" + (x.a == -1 ? "-" : x.a));
        }

        try (PrintWriter pw = new PrintWriter("littab.txt")) {
            pw.println("Index\tLiteral\tAddress");
            for (Lit x : LT)
                pw.println(x.i + "\t" + x.r + "\t" + (x.a == -1 ? "-" : x.a));
        }

        try (PrintWriter pw = new PrintWriter("pooltab.txt")) {
            pw.println("--- POOL TABLE ---");
            pw.println("Index");
            for (Integer p : PT)
                pw.println(p);
        }

        System.out.println("PASS 1 completed ✅");
        System.out.println("Generated: intermediate.txt, symtab.txt, littab.txt, pooltab.txt");
    }

    // === MAIN FUNCTION ===
    public static void main(String[] args) {
        String input = "input.txt";
        if (args.length > 0)
            input = args[0];

        try {
            new Pass1Assembler().run(input);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
