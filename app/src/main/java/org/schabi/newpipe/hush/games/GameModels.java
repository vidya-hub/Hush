package org.schabi.newpipe.hush.games;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** Pure game rules; UI and Android lifecycle code live in GamesFragment. */
public final class GameModels {
    private GameModels() { }

    public static final class Twenty48 {
        public final int[] board = new int[16];
        public int score;
        public int best;
        public boolean won;
        public boolean acknowledged;
        public boolean gameOver;
        private int[] undoBoard;
        private int undoScore;
        private final Random random;

        public Twenty48(final Random random) {
            this.random = random;
            reset();
        }

        public void reset() {
            Arrays.fill(board, 0);
            score = 0;
            won = false;
            acknowledged = false;
            gameOver = false;
            undoBoard = null;
            spawn();
            spawn();
        }

        public static final class TileMotion {
            public final int from, to, value;
            public final boolean merged;
            TileMotion(int from, int to, int value, boolean merged) {
                this.from=from; this.to=to; this.value=value; this.merged=merged;
            }
        }
        public static final class MoveResult {
            public final boolean changed;
            public final List<TileMotion> motions;
            private final int[] before, after;
            public final int spawnIndex, scoreDelta;
            MoveResult(boolean changed, List<TileMotion> motions, int[] before, int[] after, int spawnIndex, int delta) {
                this.changed=changed; this.motions=Collections.unmodifiableList(new ArrayList<>(motions));
                this.before=before.clone(); this.after=after.clone(); this.spawnIndex=spawnIndex; scoreDelta=delta;
            }
            public int[] before() { return before.clone(); }
            public int[] after() { return after.clone(); }
        }
        public boolean move(final int direction) { return moveDetailed(direction).changed; }
        public MoveResult moveDetailed(final int direction) {
            final int[] before=board.clone(); final int oldScore=score;
            if (direction < 0 || direction > 3 || gameOver) return new MoveResult(false,Collections.emptyList(),before,before,-1,0);
            final List<TileMotion> motions=new ArrayList<>();
            for(int line=0;line<4;line++) {
                int[] values=new int[4], sources=new int[4]; int count=0;
                for(int pos=0;pos<4;pos++) {
                    int source=index(line,pos,direction);
                    if(board[source]!=0) {values[count]=board[source];sources[count++]=source;}
                }
                int[] merged=new int[4]; int out=0;
                for(int i=0;i<count;i++) {
                    int value=values[i]; int to=index(line,out,direction);
                    boolean combines=i+1<count && value==values[i+1];
                    motions.add(new TileMotion(sources[i],to,value,combines));
                    if(combines) {
                        motions.add(new TileMotion(sources[i+1],to,value,true));value*=2;score+=value;
                        if(value>=2048)won=true;i++;
                    }
                    merged[out++]=value;
                }
                for(int pos=0;pos<4;pos++)board[index(line,pos,direction)]=merged[pos];
            }
            if(Arrays.equals(before,board)) {
                gameOver=!canMove(); return new MoveResult(false,Collections.emptyList(),before,before,-1,0);
            }
            undoBoard=before;undoScore=oldScore;best=Math.max(best,score);
            int[] settled=board.clone();spawn();int spawnIndex=-1;
            for(int i=0;i<16;i++)if(settled[i]!=board[i])spawnIndex=i;
            gameOver=!canMove();return new MoveResult(true,motions,before,board,spawnIndex,score-oldScore);
        }

        private int index(final int line, final int pos, final int direction) {
            switch (direction) {
                case 0: return line * 4 + pos; // left
                case 1: return line * 4 + 3 - pos; // right
                case 2: return pos * 4 + line; // up
                default: return (3 - pos) * 4 + line; // down
            }
        }

        private void spawn() {
            final List<Integer> empty = new ArrayList<>();
            for (int i = 0; i < 16; i++) if (board[i] == 0) empty.add(i);
            if (!empty.isEmpty()) board[empty.get(random.nextInt(empty.size()))] =
                    random.nextInt(10) == 0 ? 4 : 2;
        }

        public boolean canMove() {
            for (int i = 0; i < 16; i++) {
                if (board[i] == 0) return true;
                if (i % 4 < 3 && board[i] == board[i + 1]) return true;
                if (i / 4 < 3 && board[i] == board[i + 4]) return true;
            }
            return false;
        }

        public boolean canUndo() { return undoBoard != null; }

        public boolean undo() {
            if (undoBoard == null) return false;
            System.arraycopy(undoBoard, 0, board, 0, 16);
            score = undoScore;
            undoBoard = null;
            won = Arrays.stream(board).anyMatch(value -> value >= 2048);
            acknowledged = won && acknowledged;
            gameOver = !canMove();
            return true;
        }

        public JSONObject save() {
            final JSONObject state = new JSONObject();
            try {
                state.put("board", array(board));
                state.put("score", score);
                state.put("best", best);
                state.put("won", won);
                state.put("ack", acknowledged);
                state.put("over", gameOver);
                if (undoBoard != null) {
                    state.put("undo", array(undoBoard));
                    state.put("undoScore", undoScore);
                }
            } catch (final JSONException ignored) { }
            return state;
        }

        public void load(final JSONObject state) {
            final JSONArray saved = state.optJSONArray("board");
            if (saved == null || saved.length() != 16) return;
            for (int i = 0; i < 16; i++) board[i] = saved.optInt(i);
            score = state.optInt("score");
            best = state.optInt("best");
            won = state.optBoolean("won");
            acknowledged = state.optBoolean("ack");
            gameOver = state.optBoolean("over");
            final JSONArray old = state.optJSONArray("undo");
            if (old != null && old.length() == 16) {
                undoBoard = new int[16];
                for (int i = 0; i < 16; i++) undoBoard[i] = old.optInt(i);
                undoScore = state.optInt("undoScore");
            }
        }
    }

    public static final class Snake {
        public static final int SIZE = 18;
        public final ArrayDeque<Integer> body = new ArrayDeque<>();
        public int direction = 1; // 0 up, 1 right, 2 down, 3 left
        private final ArrayDeque<Integer> turns = new ArrayDeque<>(2);
        public boolean won;
        public int nextDirection = 1;
        public int food;
        public int score;
        public int best;
        public boolean alive = true;
        public boolean paused = true;
        private final Random random;

        public Snake(final Random random) {
            this.random = random;
            reset();
        }

        public void reset() {
            body.clear();
            body.add(9 * SIZE + 7);
            body.add(9 * SIZE + 8);
            body.add(9 * SIZE + 9);
            direction = 1;
            nextDirection = 1;
            turns.clear();
            won = false;
            score = 0;
            alive = true;
            paused = true;
            placeFood();
        }

        public void turn(final int chosen) {
            int previous = turns.isEmpty() ? direction : turns.peekLast();
            if (!alive || turns.size() >= 2 || chosen < 0 || chosen > 3
                    || chosen == previous || (chosen + 2) % 4 == previous) return;
            turns.addLast(chosen);
            nextDirection = turns.peekFirst();
        }

        public boolean tick() {
            if (!alive || paused) return false;
            direction = turns.isEmpty() ? nextDirection : turns.removeFirst();
            nextDirection = turns.isEmpty() ? direction : turns.peekFirst();
            final int head = body.peekLast();
            final int row = head / SIZE + (direction == 0 ? -1 : direction == 2 ? 1 : 0);
            final int col = head % SIZE + (direction == 1 ? 1 : direction == 3 ? -1 : 0);
            if (row < 0 || row >= SIZE || col < 0 || col >= SIZE) {
                alive = false;
                paused = true;
                return false;
            }
            final int next = row * SIZE + col;
            final boolean eats = next == food;
            final int tail = body.peekFirst();
            if (body.contains(next) && (eats || next != tail)) {
                alive = false;
                paused = true;
                return false;
            }
            body.addLast(next);
            if (eats) {
                score++;
                best = Math.max(best, score);
                if (body.size() == SIZE * SIZE) {
                    won = true;
                    alive = false;
                    paused = true;
                } else {
                    placeFood();
                }
            } else {
                body.removeFirst();
            }
            return true;
        }

        private void placeFood() {
            boolean[] occupied = new boolean[SIZE * SIZE];
            for (int cell : body) occupied[cell] = true;
            int free = SIZE * SIZE - body.size();
            if (free <= 0) return;
            int chosen = random.nextInt(free);
            for (int cell = 0; cell < occupied.length; cell++) {
                if (!occupied[cell] && chosen-- == 0) {food = cell;return;}
            }
        }

        public JSONObject save() {
            final JSONObject state = new JSONObject();
            try {
                final JSONArray cells = new JSONArray();
                for (final int cell : body) cells.put(cell);
                state.put("body", cells);
                state.put("direction", direction);
                state.put("next", nextDirection);
                JSONArray pending = new JSONArray();
                for (int turn : turns) pending.put(turn);
                state.put("turns", pending);
                state.put("won", won);
                state.put("food", food);
                state.put("score", score);
                state.put("best", best);
                state.put("alive", alive);
            } catch (final JSONException ignored) { }
            return state;
        }

        public void load(final JSONObject state) {
            best = Math.max(best, Math.max(0,state.optInt("best")));
            final JSONArray cells = state.optJSONArray("body");
            if (cells == null || cells.length() < 3 || cells.length() > SIZE * SIZE) return;
            boolean[] occupied = new boolean[SIZE * SIZE];
            int previous = -1;
            for (int i = 0; i < cells.length(); i++) {
                int cell = cells.optInt(i,-1);
                if (cell < 0 || cell >= occupied.length || occupied[cell]
                        || previous >= 0 && Math.abs(cell / SIZE - previous / SIZE)
                        + Math.abs(cell % SIZE - previous % SIZE) != 1) return;
                occupied[cell] = true;previous = cell;
            }
            int savedDirection = state.optInt("direction",1);
            if (savedDirection < 0 || savedDirection > 3) return;
            body.clear();
            for (int i = 0; i < cells.length(); i++) body.add(cells.optInt(i));
            direction = savedDirection;nextDirection = direction;turns.clear();
            won = body.size() == SIZE * SIZE;
            alive = !won && state.optBoolean("alive",true);
            final JSONArray pending = state.optJSONArray("turns");
            if (pending != null) {
                for (int i = 0; i < Math.min(2,pending.length()); i++) turn(pending.optInt(i,-1));
            } else turn(state.optInt("next",direction));
            food = state.optInt("food",-1);
            if (!won && (food < 0 || food >= SIZE * SIZE || occupied[food])) placeFood();
            score = Math.max(0,state.optInt("score"));best = Math.max(best,score);
            paused = true;
        }
    }

    public static final class Sudoku {
        private static final String[] PUZZLES = {
                "530070000600195000098000060800060003400803001700020006060000280000419005000080079",
                "000260701680070090190004500820100040004602900050003028009300074040050036703018000"
        };
        private static final String[] SOLUTIONS = {
                "534678912672195348198342567859761423426853791713924856961537284287419635345286179",
                "435269781682571493197834562826195347374682915951743628519326874248957136763418259"
        };
        public final int[] clues = new int[81];
        public final int[] cells = new int[81];
        public final int[] solution = new int[81];
        public final int[] notes = new int[81];
        public int difficulty;
        public String puzzleId;
        public int selected = -1;
        public long elapsedMs;
        public boolean paused = true;
        private long resumedAt;
        private final Random random;

        public Sudoku(final Random random) {
            this.random = random;
            newPuzzle(0);
        }

        public void newPuzzle(final int level) {
            difficulty = level == 1 ? 1 : 0;
            final int base = difficulty == 0 ? 1 : 0;
            final int[] digits = {1,2,3,4,5,6,7,8,9};
            for (int i = digits.length - 1; i > 0; i--) {
                final int j = random.nextInt(i + 1);
                final int swap = digits[i]; digits[i] = digits[j]; digits[j] = swap;
            }
            final String puzzle = PUZZLES[base];
            final String solved = SOLUTIONS[base];
            final int[] rows = shuffledGroups(random);
            final int[] columns = shuffledGroups(random);
            for (int i = 0; i < 81; i++) {
                final int source = rows[i / 9] * 9 + columns[i % 9];
                final int given = puzzle.charAt(source) - '0';
                clues[i] = given == 0 ? 0 : digits[given - 1];
                solution[i] = digits[solved.charAt(source) - '1'];
                cells[i] = clues[i];
                notes[i] = 0;
            }
            puzzleId = "v1-" + difficulty + "-" + Integer.toHexString(Arrays.hashCode(clues));
            selected = -1;
            elapsedMs = 0;
            paused = true;
        }

        private static int[] shuffledGroups(Random random) {
            List<Integer> bands=new ArrayList<>(Arrays.asList(0,1,2));Collections.shuffle(bands,random);
            int[] order=new int[9];int offset=0;
            for(int band:bands) {
                List<Integer> within=new ArrayList<>(Arrays.asList(0,1,2));Collections.shuffle(within,random);
                for(int item:within)order[offset++]=band*3+item;
            }
            return order;
        }

        public void set(final int index, final int value, final boolean pencil) {
            if (index < 0 || index >= 81 || clues[index] != 0 || value < 1 || value > 9) return;
            if (pencil) {
                notes[index] ^= 1 << value;
            } else {
                cells[index] = value;
                notes[index] = 0;
            }
        }

        public void erase(final int index) {
            if (index < 0 || index >= 81 || clues[index] != 0) return;
            cells[index] = 0;
            notes[index] = 0;
        }

        public void hint() {
            int index = selected;
            if (index < 0 || clues[index] != 0 || cells[index] == solution[index]) {
                index = -1;
                for (int i = 0; i < 81; i++) {
                    if (cells[i] != solution[i]) { index = i; break; }
                }
            }
            if (index >= 0) {
                cells[index] = solution[index];
                notes[index] = 0;
                selected = index;
            }
        }

        public boolean conflict(final int index) {
            final int value = cells[index];
            if (value == 0) return false;
            final int row = index / 9;
            final int col = index % 9;
            for (int i = 0; i < 81; i++) {
                if (i == index || cells[i] != value) continue;
                if (i / 9 == row || i % 9 == col
                        || (i / 27 == row / 3 && (i % 9) / 3 == col / 3)) return true;
            }
            return false;
        }

        public boolean complete() {
            return Arrays.equals(cells, solution);
        }

        public void resume(final long now) {
            if (!paused || complete()) return;
            paused = false;
            resumedAt = now;
        }

        public void pause(final long now) {
            if (!paused) {
                elapsedMs += Math.max(0, now - resumedAt);
                paused = true;
            }
        }

        public long elapsed(final long now) {
            return elapsedMs + (paused ? 0 : Math.max(0, now - resumedAt));
        }

        public JSONObject save(final long now) {
            final JSONObject state = new JSONObject();
            try {
                state.put("clues", array(clues));
                state.put("cells", array(cells));
                state.put("solution", array(solution));
                state.put("notes", array(notes));
                state.put("difficulty", difficulty);
                state.put("puzzleId", puzzleId);
                state.put("selected", selected);
                state.put("elapsed", elapsed(now));
            } catch (final JSONException ignored) { }
            return state;
        }

        public void load(final JSONObject state) {
            final JSONArray saved = state.optJSONArray("cells");
            final JSONArray given = state.optJSONArray("clues");
            final JSONArray solved = state.optJSONArray("solution");
            if (saved == null || given == null || solved == null
                    || saved.length() != 81 || given.length() != 81 || solved.length() != 81) return;
            final JSONArray savedNotes = state.optJSONArray("notes");
            for (int i = 0; i < 81; i++) {
                clues[i] = given.optInt(i);
                cells[i] = saved.optInt(i);
                solution[i] = solved.optInt(i);
                notes[i] = savedNotes == null ? 0 : savedNotes.optInt(i);
            }
            difficulty = state.optInt("difficulty");
            puzzleId = state.optString("puzzleId", "v1-" + difficulty + "-" + Integer.toHexString(Arrays.hashCode(clues)));
            elapsedMs = state.optLong("elapsed");
            paused = true;
            selected = state.optInt("selected", -1);
            if (selected < -1 || selected >= 81) selected = -1;
        }
    }

    public static final class Make24 {
        public static final char[] OPERATIONS = {'+', '−', '×', '÷'};
        public final List<Term> terms = new ArrayList<>();
        private final ArrayDeque<String> history = new ArrayDeque<>();
        private final Random random;
        public boolean solved;

        public Make24(final Random random) {
            this.random = random;
            next();
        }

        public void next() {
            terms.clear();
            history.clear();
            solved = false;
            for (int attempt = 0; attempt < 1000; attempt++) {
                final List<Term> candidate = new ArrayList<>();
                for (int i = 0; i < 4; i++) {
                    final int number = random.nextInt(9) + 1;
                    candidate.add(new Term(number, 1, Integer.toString(number)));
                }
                if (solvable(candidate)) {
                    terms.addAll(candidate);
                    return;
                }
            }
            terms.addAll(Arrays.asList(new Term(1,1,"1"), new Term(3,1,"3"),
                    new Term(4,1,"4"), new Term(6,1,"6")));
        }

        public boolean combine(final int first, final int second, final int operation) {
            if (first == second || first < 0 || second < 0 || first >= terms.size()
                    || second >= terms.size() || operation < 0 || operation > 3 || solved) return false;
            final Term result = operation(terms.get(first), terms.get(second), operation);
            if (result == null) return false;
            history.push(serializeTerms());
            final List<Term> remaining = new ArrayList<>();
            for (int i = 0; i < terms.size(); i++) {
                if (i != first && i != second) remaining.add(terms.get(i));
            }
            remaining.add(result);
            terms.clear();
            terms.addAll(remaining);
            solved = terms.size() == 1 && terms.get(0).num == 24 * terms.get(0).den;
            return true;
        }

        public boolean canUndo() { return !history.isEmpty(); }

        public boolean undo() {
            if (history.isEmpty()) return false;
            restoreTerms(history.pop());
            solved = false;
            return true;
        }

        public String hint() {
            for (int i = 0; i < terms.size(); i++) {
                for (int j = 0; j < terms.size(); j++) {
                    if (i == j) continue;
                    for (int op = 0; op < 4; op++) {
                        final Term result = operation(terms.get(i), terms.get(j), op);
                        if (result == null) continue;
                        final List<Term> next = new ArrayList<>();
                        for (int k = 0; k < terms.size(); k++) {
                            if (k != i && k != j) next.add(terms.get(k));
                        }
                        next.add(result);
                        if (solvable(next)) {
                            return terms.get(i).expression + " " + OPERATIONS[op] + " "
                                    + terms.get(j).expression;
                        }
                    }
                }
            }
            return "Try Undo for another path";
        }

        public static boolean solvable(final List<Term> values) {
            if (values.size() == 1) {
                return values.get(0).num == 24 * values.get(0).den;
            }
            for (int i = 0; i < values.size(); i++) {
                for (int j = 0; j < values.size(); j++) {
                    if (i == j) continue;
                    for (int op = 0; op < 4; op++) {
                        final Term combined = operation(values.get(i), values.get(j), op);
                        if (combined == null) continue;
                        final List<Term> next = new ArrayList<>();
                        for (int k = 0; k < values.size(); k++) {
                            if (k != i && k != j) next.add(values.get(k));
                        }
                        next.add(combined);
                        if (solvable(next)) return true;
                    }
                }
            }
            return false;
        }

        private static Term operation(final Term left, final Term right, final int op) {
            final long numerator;
            final long denominator;
            switch (op) {
                case 0:
                    numerator = left.num * right.den + right.num * left.den;
                    denominator = left.den * right.den;
                    break;
                case 1:
                    numerator = left.num * right.den - right.num * left.den;
                    denominator = left.den * right.den;
                    break;
                case 2:
                    numerator = left.num * right.num;
                    denominator = left.den * right.den;
                    break;
                default:
                    if (right.num == 0) return null;
                    numerator = left.num * right.den;
                    denominator = left.den * right.num;
            }
            return new Term(numerator, denominator, "(" + left.expression + " "
                    + OPERATIONS[op] + " " + right.expression + ")");
        }

        public JSONObject save() {
            final JSONObject state = new JSONObject();
            try {
                state.put("terms", new JSONArray(serializeTerms()));
                final JSONArray old = new JSONArray();
                for (final String value : history) old.put(value);
                state.put("history", old);
                state.put("solved", solved);
            } catch (final JSONException ignored) { }
            return state;
        }

        public void load(final JSONObject state) {
            final JSONArray saved = state.optJSONArray("terms");
            if (saved == null || saved.length() == 0) return;
            restoreTerms(saved.toString());
            history.clear();
            final JSONArray old = state.optJSONArray("history");
            if (old != null) {
                for (int i = old.length() - 1; i >= 0; i--) history.push(old.optString(i));
            }
            solved = state.optBoolean("solved");
        }

        private String serializeTerms() {
            final JSONArray saved = new JSONArray();
            for (final Term term : terms) {
                final JSONObject item = new JSONObject();
                try {
                    item.put("num", term.num);
                    item.put("den", term.den);
                    item.put("expression", term.expression);
                } catch (final JSONException ignored) { }
                saved.put(item);
            }
            return saved.toString();
        }

        private void restoreTerms(final String raw) {
            try {
                final JSONArray saved = new JSONArray(raw);
                terms.clear();
                for (int i = 0; i < saved.length(); i++) {
                    final JSONObject item = saved.getJSONObject(i);
                    terms.add(new Term(item.getLong("num"), item.getLong("den"),
                            item.getString("expression")));
                }
            } catch (final JSONException ignored) { }
        }

        public static final class Term {
            public final long num;
            public final long den;
            public final String expression;
            public Term(final long numerator, final long denominator, final String text) {
                final long divisor = gcd(Math.abs(numerator), Math.abs(denominator));
                final long sign = denominator < 0 ? -1 : 1;
                num = sign * numerator / divisor;
                den = sign * denominator / divisor;
                expression = text;
            }
            @Override public String toString() {
                return den == 1 ? Long.toString(num) : num + "/" + den;
            }
            private static long gcd(long a, long b) {
                while (b != 0) { final long t = a % b; a = b; b = t; }
                return Math.max(1, a);
            }
        }
    }

    private static JSONArray array(final int[] numbers) {
        final JSONArray values = new JSONArray();
        for (final int number : numbers) values.put(number);
        return values;
    }
}
