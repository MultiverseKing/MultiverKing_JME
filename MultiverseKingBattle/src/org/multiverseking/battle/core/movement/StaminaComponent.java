/*
 * Copyright (C) 2016 roah
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.multiverseking.battle.core.movement;

import com.simsilica.es.EntityComponent;

/**
 *
 * @author roah
 */
public class StaminaComponent implements EntityComponent {

    private final float value;

    public StaminaComponent() {
        value = 0;
    }

    public StaminaComponent(float value) {
        this.value = value;
    }

    public float getValue() {
        return value;
    }

    /**
     * Clone component with modifiate rotation.
     *
     * @param value new value.
     * @return the cloned component.
     */
    public StaminaComponent clone(float value) {
        return new StaminaComponent(value);
    }
}
