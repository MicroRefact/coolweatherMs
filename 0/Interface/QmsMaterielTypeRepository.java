public interface QmsMaterielTypeRepository {

   public List<QmsMaterielType> findByMaterielTypeCdAndFlagStatus(String materielTypeCd,String string);
   public Object save(Object Object);
   public Object findById(Object Object);
}