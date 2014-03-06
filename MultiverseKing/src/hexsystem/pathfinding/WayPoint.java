package hexsystem.pathfinding;

import utility.HexCoordinate;

/**
 *
 * @author Eike Foede
 */
public class WayPoint {

    public HexCoordinate thisPoint;
    public WayPoint lastPoint;

    public WayPoint(HexCoordinate thisPoint, WayPoint lastPoint) {
        this.thisPoint = thisPoint;
        this.lastPoint = lastPoint;
    }
}