public interface QmsProductionInspectionRepository {

   public List<QmsProductionInspection> findByBomTechnologyIdAndFlagStatus(Integer BomTechnologyId,String FlagStatus);
}