package name.modid;

public interface ItemDisplayHpData {
    boolean solidDisplays$hasData();

    float solidDisplays$getHp();

    float solidDisplays$getMaxHp();

    void solidDisplays$setData(float hp, float maxHp);

    void solidDisplays$clearData();
}
