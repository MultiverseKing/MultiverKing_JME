/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hexsystem.chunksystem;

import archives.MeshManagerV3;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.texture.Texture;
import hexsystem.HexSettings;
import utility.HexCoordinate;
import utility.Vector2Int;

/**
 *
 * @author roah
 */
class ChunkSpatial {

    private final MeshManagerV3 meshManager;
    private final AssetManager assetManager;
    private Geometry[][] geo;

    ChunkSpatial(MeshManagerV3 meshManager, AssetManager assetManager) {
        this.meshManager = meshManager;
        this.assetManager = assetManager;
    }

    /**
     * Generate all geometry of chunk with default parameter.
     *
     * @param rootChunk root Node
     * @param hexSettings settings to use.
     * @param subChunkSize size of a subChunk.
     * @param chunkControl root control.
     */
    void initialize(Node rootChunk, HexSettings hexSettings, int subChunkSize, MeshParameter meshParam) {
        int subChunkCount = hexSettings.getCHUNK_SIZE() / subChunkSize;
        geo = new Geometry[subChunkCount][subChunkCount];
        Material mat = assetManager.loadMaterial("Materials/hexMat.j3m");
        Texture text = assetManager.loadTexture("Textures/Test/testPattern_01.png");
        text.setWrap(Texture.WrapMode.Repeat);
        mat.setTexture("ColorMap", text);
//        mat.getAdditionalRenderState().setWireframe(true);
        mat.getAdditionalRenderState().setDepthWrite(true);

        for (int y = 0; y < subChunkCount; y++) {
            for (int x = 0; x < subChunkCount; x++) {
                Vector2Int subChunkLocalChunkPos = rootChunk.getControl(ChunkControl.class).getSubChunkLocalChunkPos(new HexCoordinate(HexCoordinate.OFFSET, x * subChunkSize, y * subChunkSize));
                HexCoordinate subChunkHexWorldPos = getSubChunkHexWorldPos(subChunkLocalChunkPos, subChunkSize);
                meshParam.initialize(subChunkSize, subChunkHexWorldPos, false);
                geo[x][y] = new Geometry(Integer.toString(x) + "|" + Integer.toString(y), meshManager.getMergedMesh(meshParam));
                rootChunk.attachChild(geo[x][y]);
                geo[x][y].setLocalTranslation(getSubChunkLocalWorldPosition(x, y, hexSettings, subChunkSize));

                geo[x][y].setMaterial(mat);
            }
        }
    }

    /**
     * @todo custom cull
     * @param enabled
     */
    void setEnabled(boolean enabled) {
        CullHint culling = CullHint.Inherit;
        if (!enabled) {
            culling = Spatial.CullHint.Always;
        }
        for (int x = 0; x < geo.length; x++) {
            for (int y = 0; y < geo[x].length; y++) {
                geo[x][y].setCullHint(culling);
            }
        }
    }

    void updateSubChunk(Vector2Int subChunkLocalGridPos, MeshParameter meshParam) {
        this.geo[subChunkLocalGridPos.x][subChunkLocalGridPos.y].setMesh(meshManager.getMergedMesh(meshParam));
    }

    /**
     * Convert subChunk local grid position to world position.
     *
     * @param subChunklocalGridPos
     * @return world position of this subChunk.
     * @deprecated no use of it
     */
    Vector3f getSubChunkWorldPos(Vector2Int subChunkLocalGridPos) {
        return geo[subChunkLocalGridPos.x][subChunkLocalGridPos.y].getWorldTranslation();
    }

    /**
     * Convert SubChunk local chunk position to local world unit position,
     * relative to chunkNode.
     *
     * @param subChunkLocaGridPosX
     * @param subChunkLocalGridPosY
     * @return world unit position of the subchunk relative to his parent.
     */
    private Vector3f getSubChunkLocalWorldPosition(int subChunkLocaGridPosX, int subChunkLocalGridPosY, HexSettings hexSettings, int subChunkSize) {
        float resultX = (subChunkLocaGridPosX * subChunkSize) * hexSettings.getHEX_WIDTH() + (hexSettings.getHEX_WIDTH() / 2);
        float resultY = 0;
        float resultZ = (subChunkLocalGridPosY * subChunkSize) * (float) (hexSettings.getHEX_RADIUS() * 1.5);

        return new Vector3f(resultX, resultY, resultZ);
    }

    /**
     * @return Subchunk position in hexMap.
     */
    HexCoordinate getSubChunkHexWorldPos(Vector2Int subChunkLocalGridPos, int subChunkSize) {
        return new HexCoordinate(HexCoordinate.OFFSET, subChunkLocalGridPos.x * subChunkSize, subChunkLocalGridPos.y * subChunkSize);
    }
}
