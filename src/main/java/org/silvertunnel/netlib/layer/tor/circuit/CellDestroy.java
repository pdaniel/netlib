/**
 * OnionCoffee - Anonymous Communication through TOR Network
 * Copyright (C) 2005-2007 RWTH Aachen University, Informatik IV
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 */
package org.silvertunnel.netlib.layer.tor.circuit;

/**
 * used to create a DESTROY cell
 * 
 * @author Lexi Pimenidis
 */
class CellDestroy extends Cell {
    /**
     * creates a DESTROY-CELL
     * 
     * @param c
     *            the circuit that is to be torn down
     */
    CellDestroy(Circuit c) {
        super(c, Cell.CELL_DESTROY);
    }
}
