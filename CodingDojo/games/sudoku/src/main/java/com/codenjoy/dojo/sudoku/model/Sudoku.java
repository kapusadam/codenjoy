package com.codenjoy.dojo.sudoku.model;

/*-
 * #%L
 * Codenjoy - it's a dojo-like platform from developers to developers.
 * %%
 * Copyright (C) 2018 Codenjoy
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */


import com.codenjoy.dojo.services.Point;
import com.codenjoy.dojo.services.printer.BoardReader;
import com.codenjoy.dojo.sudoku.services.Events;

import java.util.LinkedList;
import java.util.List;

public class Sudoku implements Field {

    private List<Cell> cells;
    private Player player;

    private final int size;
    private List<Wall> walls;

    private List<Cell> acts;
    private Cell act;
    private boolean gameOver;

    public Sudoku(Level level) {
        cells = level.getCells();
        walls = level.getWalls();
        size = level.getSize();
        acts = new LinkedList<Cell>();
        gameOver = false;
    }

    @Override
    public void tick() {
        if (gameOver) return;
        if (act == null) return;

        int index = cells.indexOf(act);
        if (index == -1) throw new IllegalArgumentException("Такой координаты нет: " + act);

        Cell cell = cells.get(index);
        if (cell.isHidden()) {
            if (acts.contains(act)) {
                acts.remove(act);
            }
            acts.add(act);
            if (cell.getNumber() == act.getNumber()) {
                player.event(Events.SUCCESS);
            } else {
                player.event(Events.FAIL);
            }
        }
        act = null;

        checkIsWin();
    }

    private void checkIsWin() {
        for (Cell cell : cells) {
            if (!cell.isHidden()) continue;

            if (!acts.contains(cell)) return;

            Cell act = acts.get(acts.indexOf(cell));
            if (act.getNumber() != cell.getNumber()) {
                return;
            }
        }

        gameOver = true;
        player.event(Events.WIN);
    }

    public int getSize() {
        return size;
    }

    @Override
    public void newGame(Player player) {
        this.player = player;
        this.acts.clear();
        gameOver = false;
        player.newHero(this);
    }

    public void remove(Player player) {
        this.player = null;
    }

    public List<Cell> getCells() {
        List<Cell> result = new LinkedList<Cell>();

        for (Cell cell : cells) {
            if (acts.contains(cell)) {
                result.add(acts.get(acts.indexOf(cell)));
            } else {
                result.add(cell);
            }
        }

        return result;
    }

    @Override
    public boolean isGameOver() {
        return gameOver;
    }

    @Override
    public void gameOver() {
        player.event(Events.LOOSE);
        this.gameOver = true;
    }

    public List<Wall> getWalls() {
        return walls;
    }

    @Override
    public void set(Point pt, int n) {
        this.act = new Cell(pt, n, true);
    }

    public BoardReader reader() {
        return new BoardReader() {
            private int size = Sudoku.this.size;

            @Override
            public int size() {
                return size;
            }

            @Override
            public Iterable<? extends Point> elements() {
                return new LinkedList<Point>(){{
                    addAll(Sudoku.this.walls);
                    addAll(Sudoku.this.cells);
                    addAll(Sudoku.this.acts);
                }};
            }
        };
    }

    public Cell getAct() {
        return act;
    }
}
