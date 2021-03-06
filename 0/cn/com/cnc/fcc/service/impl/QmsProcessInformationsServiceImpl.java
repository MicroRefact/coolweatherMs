import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import org.hibernate.transform.ResultTransformer;
import org.hibernate.transform.Transformers;
import org.hibernate.type.StandardBasicTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import cn.com.cnc.fcc.domain.QmsBomTechnology;
import cn.com.cnc.fcc.domain.QmsEnclosure;
import cn.com.cnc.fcc.domain.QmsPartsAssemblyRelation;
import cn.com.cnc.fcc.domain.QmsProcess;
import cn.com.cnc.fcc.domain.QmsQualityControlDetails;
import cn.com.cnc.fcc.repository.QmsBomTechnologyRepository;
import cn.com.cnc.fcc.repository.QmsEnclosureRepository;
import cn.com.cnc.fcc.repository.QmsPartsAssemblyRelationRepository;
import cn.com.cnc.fcc.repository.QmsProcessRepository;
import cn.com.cnc.fcc.repository.QmsQualityControlDetailsRepository;
import cn.com.cnc.fcc.service.QmsProcessInformationsService;
import cn.com.cnc.fcc.service.dto.BomTechnologGroupDTO;
import cn.com.cnc.fcc.service.dto.DropDowmValueDTO;
import cn.com.cnc.fcc.service.dto.ProcessInfomationsRightListDTO;
import cn.com.cnc.fcc.service.dto.QmsOrganizationInfoDTO;
import cn.com.cnc.fcc.service.dto.QmsPartsAssemblyRelationOwnerDTO;
import cn.com.cnc.fcc.service.util.DateUtil;
@Service
public class QmsProcessInformationsServiceImpl implements QmsProcessInformationsService,cn.com.cnc.fcc.service.QmsProcessInformationsService{

 private  Logger log;

@Resource
 private  DateUtil dateUtil;

 private  EntityManagerFactory emf;

 private  QmsBomTechnologyRepository qmsBomTechnologyRepository;

 private  QmsQualityControlDetailsRepository qmsQualityControlDetailsRepository;

 private  QmsPartsAssemblyRelationRepository qmsPartsAssemblyRelationRepository;

 private  QmsEnclosureRepository qmsEnclosureRepository;

 private  QmsProcessRepository qmsProcessRepository;


@Override
@Transactional
public Integer updateInfo(BomTechnologGroupDTO bomTechnologGroupDTO){
    // ????????????
    Integer backResult = 0;
    // session??????????????????
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    // ??????????????????
    UserDetails user = (UserDetails) authentication.getPrincipal();
    // ??????????????????
    ZonedDateTime nowTime = dateUtil.getDBNowDate();
    try {
        // ?????????????????????????????????????????????
        List<QmsBomTechnology> existenceCheckQBT = qmsBomTechnologyRepository.findByMaterielIdAndTechnologyCdAndOrderNoAndFlagStatusAndIdNot(bomTechnologGroupDTO.getQmsBomTechnology().getMaterielId(), bomTechnologGroupDTO.getQmsBomTechnology().getTechnologyCd(), bomTechnologGroupDTO.getQmsBomTechnology().getOrderNo(), "0", bomTechnologGroupDTO.getQmsBomTechnology().getId());
        // ??????????????????????????????????????????????????????
        if (existenceCheckQBT.size() != 0) {
            // ???????????? ??????????????????????????????
            backResult = 1;
            return backResult;
        }
        // ????????????ID??????????????????
        List<QmsBomTechnology> existenceCheckIsDefault = qmsBomTechnologyRepository.findByMaterielIdAndIsDefaultAndFlagStatus(bomTechnologGroupDTO.getQmsBomTechnology().getMaterielId(), "1", "0");
        // ??????id????????????
        QmsBomTechnology getqmsBomTechnology = qmsBomTechnologyRepository.findByIdAndFlagStatus(bomTechnologGroupDTO.getQmsBomTechnology().getId(), "0");
        // ????????????????????????????????????
        if (existenceCheckIsDefault.size() == 0) {
            // ???????????????????????????1
            bomTechnologGroupDTO.getQmsBomTechnology().setIsDefault("1");
        } else {
            List<QmsBomTechnology> gyAndwl = qmsBomTechnologyRepository.findByMaterielIdAndTechnologyCdAndIsDefaultAndFlagStatus(bomTechnologGroupDTO.getQmsBomTechnology().getMaterielId(), bomTechnologGroupDTO.getQmsBomTechnology().getTechnologyCd(), "1", "0");
            if (gyAndwl.size() == 0) {
                // ???????????????????????????0
                bomTechnologGroupDTO.getQmsBomTechnology().setIsDefault("0");
            } else {
                // ???????????????????????????0
                bomTechnologGroupDTO.getQmsBomTechnology().setIsDefault("1");
            }
        }
        // ????????????
        bomTechnologGroupDTO.getQmsBomTechnology().setMakeTime(getqmsBomTechnology.getMakeTime());
        // ?????????
        bomTechnologGroupDTO.getQmsBomTechnology().setMakeUser(getqmsBomTechnology.getMakeUser());
        // ????????????
        bomTechnologGroupDTO.getQmsBomTechnology().setModifyTime(nowTime);
        // ?????????
        bomTechnologGroupDTO.getQmsBomTechnology().setModifyUser(user.getUsername());
        // ????????????
        bomTechnologGroupDTO.getQmsBomTechnology().setFlagStatus("0");
        // ?????????????????????
        QmsBomTechnology insertBackInfo = qmsBomTechnologyRepository.save(bomTechnologGroupDTO.getQmsBomTechnology());
        // ?????????????????????????????????????????????????????????????????????
        for (int a = 0; a < bomTechnologGroupDTO.getQmsQualityControlDetails().size(); a++) {
            // ??????id??????
            if (null != bomTechnologGroupDTO.getQmsQualityControlDetails().get(a).getId()) {
                QmsQualityControlDetails qmsQualityControlDetailsAdd = new QmsQualityControlDetails();
                // ??????id??????
                qmsQualityControlDetailsAdd = qmsQualityControlDetailsRepository.findByIdAndFlagStatus(bomTechnologGroupDTO.getQmsQualityControlDetails().get(a).getId(), "0");
                // ??????ID
                bomTechnologGroupDTO.getQmsQualityControlDetails().get(a).setBomTechnologyId(Integer.valueOf(bomTechnologGroupDTO.getQmsBomTechnology().getId().toString()));
                // ??????????????????????????? ???1???0
                if (null != bomTechnologGroupDTO.getQmsQualityControlDetails().get(a).getStandard()) {
                    bomTechnologGroupDTO.getQmsQualityControlDetails().get(a).setInspectionResultDiff("0");
                } else {
                    bomTechnologGroupDTO.getQmsQualityControlDetails().get(a).setInspectionResultDiff("1");
                }
                // ????????????
                bomTechnologGroupDTO.getQmsQualityControlDetails().get(a).setMakeTime(qmsQualityControlDetailsAdd.getMakeTime());
                // ?????????
                bomTechnologGroupDTO.getQmsQualityControlDetails().get(a).setMakeUser(qmsQualityControlDetailsAdd.getMakeUser());
                // ????????????
                bomTechnologGroupDTO.getQmsQualityControlDetails().get(a).setModifyTime(nowTime);
                // ?????????
                bomTechnologGroupDTO.getQmsQualityControlDetails().get(a).setModifyUser(user.getUsername());
                // ????????????
                bomTechnologGroupDTO.getQmsQualityControlDetails().get(a).setFlagStatus("0");
                // ?????????????????????????????????
                qmsQualityControlDetailsRepository.save(bomTechnologGroupDTO.getQmsQualityControlDetails().get(a));
            } else {
                // ??????ID
                bomTechnologGroupDTO.getQmsQualityControlDetails().get(a).setBomTechnologyId(Integer.valueOf(bomTechnologGroupDTO.getQmsBomTechnology().getId().toString()));
                // ??????????????????????????? ???1???0
                if (null != bomTechnologGroupDTO.getQmsQualityControlDetails().get(a).getStandard()) {
                    bomTechnologGroupDTO.getQmsQualityControlDetails().get(a).setInspectionResultDiff("0");
                } else {
                    bomTechnologGroupDTO.getQmsQualityControlDetails().get(a).setInspectionResultDiff("1");
                }
                // ????????????
                bomTechnologGroupDTO.getQmsQualityControlDetails().get(a).setMakeTime(nowTime);
                // ?????????
                bomTechnologGroupDTO.getQmsQualityControlDetails().get(a).setMakeUser(user.getUsername());
                // ????????????
                bomTechnologGroupDTO.getQmsQualityControlDetails().get(a).setModifyTime(nowTime);
                // ?????????
                bomTechnologGroupDTO.getQmsQualityControlDetails().get(a).setModifyUser(user.getUsername());
                // ????????????
                bomTechnologGroupDTO.getQmsQualityControlDetails().get(a).setFlagStatus("0");
                // ?????????????????????????????????
                qmsQualityControlDetailsRepository.save(bomTechnologGroupDTO.getQmsQualityControlDetails().get(a));
            }
        }
        // ??????????????????????????????????????????????????????
        for (int b = 0; b < bomTechnologGroupDTO.getQmsPartsAssemblyRelation().size(); b++) {
            if (null != bomTechnologGroupDTO.getQmsPartsAssemblyRelation().get(b).getId()) {
                QmsPartsAssemblyRelation partsAssemblyRelationEdit = new QmsPartsAssemblyRelation();
                // ??????id??????
                partsAssemblyRelationEdit = qmsPartsAssemblyRelationRepository.findByIdAndFlagStatus(bomTechnologGroupDTO.getQmsPartsAssemblyRelation().get(b).getId(), "0");
                // ??????ID
                partsAssemblyRelationEdit.setBomTechnologyId(bomTechnologGroupDTO.getQmsPartsAssemblyRelation().get(b).getBomTechnologyId());
                // ????????????
                partsAssemblyRelationEdit.setAssemblyNum(bomTechnologGroupDTO.getQmsPartsAssemblyRelation().get(b).getAssemblyNum());
                // ????????????ID
                partsAssemblyRelationEdit.setAssemblyMaterielId(bomTechnologGroupDTO.getQmsPartsAssemblyRelation().get(b).getAssemblyMaterielId());
                // ??????
                partsAssemblyRelationEdit.setAssemblyCount(bomTechnologGroupDTO.getQmsPartsAssemblyRelation().get(b).getAssemblyCount());
                // ??????
                partsAssemblyRelationEdit.setRemark(bomTechnologGroupDTO.getQmsPartsAssemblyRelation().get(b).getRemark());
                // ??????ID
                partsAssemblyRelationEdit.setBomTechnologyId(Integer.valueOf(bomTechnologGroupDTO.getQmsBomTechnology().getId().toString()));
                // ????????????
                partsAssemblyRelationEdit.setModifyTime(nowTime);
                // ?????????
                partsAssemblyRelationEdit.setModifyUser(user.getUsername());
                qmsPartsAssemblyRelationRepository.save(partsAssemblyRelationEdit);
            } else {
                QmsPartsAssemblyRelation partsAssemblyRelationadd = new QmsPartsAssemblyRelation();
                // ??????ID
                partsAssemblyRelationadd.setBomTechnologyId(bomTechnologGroupDTO.getQmsPartsAssemblyRelation().get(b).getBomTechnologyId());
                // ????????????
                partsAssemblyRelationadd.setAssemblyNum(bomTechnologGroupDTO.getQmsPartsAssemblyRelation().get(b).getAssemblyNum());
                // ????????????ID
                partsAssemblyRelationadd.setAssemblyMaterielId(bomTechnologGroupDTO.getQmsPartsAssemblyRelation().get(b).getAssemblyMaterielId());
                // ??????
                partsAssemblyRelationadd.setAssemblyCount(bomTechnologGroupDTO.getQmsPartsAssemblyRelation().get(b).getAssemblyCount());
                // ??????
                partsAssemblyRelationadd.setRemark(bomTechnologGroupDTO.getQmsPartsAssemblyRelation().get(b).getRemark());
                // ??????ID
                partsAssemblyRelationadd.setBomTechnologyId(Integer.valueOf(bomTechnologGroupDTO.getQmsBomTechnology().getId().toString()));
                // ????????????
                partsAssemblyRelationadd.setMakeTime(nowTime);
                // ?????????
                partsAssemblyRelationadd.setMakeUser(user.getUsername());
                // ????????????
                partsAssemblyRelationadd.setModifyTime(nowTime);
                // ?????????
                partsAssemblyRelationadd.setModifyUser(user.getUsername());
                // ????????????
                partsAssemblyRelationadd.setFlagStatus("0");
                // ??????????????????
                qmsPartsAssemblyRelationRepository.save(partsAssemblyRelationadd);
            }
        }
        // ????????????????????????ID
        backResult = Integer.valueOf(insertBackInfo.getId().toString());
    } catch (Exception e) {
        // ????????????
        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        backResult = 2;
        return backResult;
    }
    return backResult;
}


@Override
public Boolean deleteEnclosure(Integer inspectionInfoId,String inspectionKbn,String enclosureAddress){
    try {
        qmsEnclosureRepository.deleteById(Long.valueOf(inspectionInfoId));
    } catch (Exception e) {
        System.out.println(e);
        return false;
    }
    // ?????????
    return true;
}


public String getSelectAllInfo(HashMap<String,Object> param){
    StringBuffer objSqlContent = new StringBuffer();
    objSqlContent.append(" SELECT qbt.id as \"id\" , ");
    objSqlContent.append(" qbt.order_no as \"orderNo\" , ");
    objSqlContent.append(" qp.process_cd as \"processCd\",  ");
    objSqlContent.append(" qp.process_name as \"processName\"  ");
    objSqlContent.append(" FROM qms_bom_technology qbt ");
    objSqlContent.append(" LEFT JOIN qms_process qp ON qp.id = qbt.process_id AND qp.flag_status = 0");
    objSqlContent.append(" WHERE qbt.flag_status = 0 ");
    objSqlContent.append(" AND qbt.materiel_id = :materielCdVague ");
    objSqlContent.append(" AND qbt.technology_cd = :technologyCd ");
    objSqlContent.append(" ORDER BY qbt.order_no ASC ");
    objSqlContent.append(" LIMIT :start , :end ");
    // ?????????
    return objSqlContent.toString();
}


@SuppressWarnings({ "unchecked", "deprecation" })
@Override
public List<QmsPartsAssemblyRelationOwnerDTO> getFlagStatusAndBomTechnologyId(String FlagStatus,Integer BomTechnologyId){
    // ??????????????????
    List<QmsPartsAssemblyRelationOwnerDTO> QmsPartsAssemblyRelation = new ArrayList<QmsPartsAssemblyRelationOwnerDTO>();
    // ??????????????????
    EntityManager em = emf.createEntityManager();
    try {
        // ??????
        Query query = em.createNativeQuery(getBomTechnologyIdAllInfo(FlagStatus, BomTechnologyId));
        ResultTransformer transformer = Transformers.aliasToBean(QmsPartsAssemblyRelationOwnerDTO.class);
        query.setParameter("BomTechnologyId", BomTechnologyId);
        query.setParameter("FlagStatus", FlagStatus);
        QmsPartsAssemblyRelation = query.unwrap(org.hibernate.query.NativeQuery.class).addScalar("id", StandardBasicTypes.LONG).addScalar("bomTechnologyId", StandardBasicTypes.INTEGER).addScalar("assemblyNum", StandardBasicTypes.INTEGER).addScalar("assemblyMaterielId", StandardBasicTypes.INTEGER).addScalar("assemblyMaterielCd", StandardBasicTypes.STRING).addScalar("assemblyMaterielName", StandardBasicTypes.STRING).addScalar("assemblyCount", StandardBasicTypes.INTEGER).addScalar("remark", StandardBasicTypes.STRING).addScalar("flagStatus", StandardBasicTypes.STRING).addScalar("compPkid", StandardBasicTypes.STRING).addScalar("reserveFirst", StandardBasicTypes.STRING).addScalar("reserveSecond", StandardBasicTypes.STRING).addScalar("reserveThird", StandardBasicTypes.STRING).addScalar("makeUser", StandardBasicTypes.STRING).addScalar("modifyUser", StandardBasicTypes.STRING).setResultTransformer(transformer).getResultList();
        query = null;
    } catch (Exception e) {
        // ????????????
        log.info(e.getMessage());
    } finally {
        em.close();
    }
    return QmsPartsAssemblyRelation;
}


@SuppressWarnings({ "unchecked", "deprecation" })
@Override
public List<QmsOrganizationInfoDTO> organListInfo(){
    // ??????????????????
    EntityManager em = emf.createEntityManager();
    List<QmsOrganizationInfoDTO> qmsOrganizationInfoDTO = new ArrayList<QmsOrganizationInfoDTO>();
    try {
        // ??????
        Query query = em.createNativeQuery(getSelectAllInfo());
        ResultTransformer transformer = Transformers.aliasToBean(QmsOrganizationInfoDTO.class);
        // ?????????DTO
        qmsOrganizationInfoDTO = query.unwrap(org.hibernate.query.NativeQuery.class).addScalar("id", StandardBasicTypes.BIG_INTEGER).addScalar("parentCd", StandardBasicTypes.STRING).addScalar("organizationCd", StandardBasicTypes.STRING).addScalar("organizationName", StandardBasicTypes.STRING).addScalar("vehicleType", StandardBasicTypes.STRING).addScalar("materielCd", StandardBasicTypes.STRING).setResultTransformer(transformer).getResultList();
        query = null;
    } catch (Exception e) {
        log.info(e.getMessage());
    } finally {
        em.close();
    }
    return qmsOrganizationInfoDTO;
}


@SuppressWarnings({ "unchecked", "deprecation" })
@Override
public List<ProcessInfomationsRightListDTO> selectAllInfo(HashMap<String,Object> param){
    List<ProcessInfomationsRightListDTO> processInfomationsRightList = new ArrayList<ProcessInfomationsRightListDTO>();
    // ????????????????????????
    String materielCdVague = param.get("materielCd").toString();
    // ????????????????????????
    String technologyCd = param.get("technologyCd").toString();
    // ???????????????
    Integer pageSize = (Integer.valueOf(param.get("sizeNumber").toString()) == 0 ? 0 : Integer.valueOf(param.get("sizeNumber").toString()));
    Integer pageNum = Integer.valueOf(param.get("pageNumber").toString());
    // ??????????????????
    EntityManager em = emf.createEntityManager();
    try {
        // ??????
        Query query = em.createNativeQuery(getSelectAllInfo(param));
        ResultTransformer transformer = Transformers.aliasToBean(ProcessInfomationsRightListDTO.class);
        query.setParameter("materielCdVague", materielCdVague);
        query.setParameter("technologyCd", technologyCd);
        query.setParameter("start", (pageNum - 1) * pageSize);
        query.setParameter("end", pageSize);
        processInfomationsRightList = query.unwrap(org.hibernate.query.NativeQuery.class).addScalar("id", StandardBasicTypes.LONG).addScalar("orderNo", StandardBasicTypes.STRING).addScalar("processCd", StandardBasicTypes.STRING).addScalar("processName", StandardBasicTypes.STRING).setResultTransformer(transformer).getResultList();
        query = null;
    } catch (Exception e) {
        // ????????????
        log.info(e.getMessage());
    } finally {
        em.close();
    }
    return processInfomationsRightList;
}


@SuppressWarnings({ "unchecked", "deprecation" })
@Override
public List<QmsOrganizationInfoDTO> getParentNodeListInfo(String materielID,String vehicleId){
    // ??????????????????
    EntityManager em = emf.createEntityManager();
    List<QmsOrganizationInfoDTO> qmsOrganizationInfoDTO = new ArrayList<QmsOrganizationInfoDTO>();
    try {
        // ??????
        Query query = em.createNativeQuery(getSelectParentNodeInfo(materielID, vehicleId));
        ResultTransformer transformer = Transformers.aliasToBean(QmsOrganizationInfoDTO.class);
        // ??????????????????????????????
        if (!"".equals(materielID)) {
            query.setParameter("materielID", "%" + materielID + "%");
        }
        // ??????????????????????????????
        if (!"".equals(vehicleId)) {
            query.setParameter("materielName", "%" + vehicleId + "%");
        }
        // ?????????DTO
        qmsOrganizationInfoDTO = query.unwrap(org.hibernate.query.NativeQuery.class).addScalar("id", StandardBasicTypes.BIG_INTEGER).addScalar("parentCd", StandardBasicTypes.STRING).addScalar("organizationCd", StandardBasicTypes.STRING).addScalar("organizationName", StandardBasicTypes.STRING).addScalar("vehicleType", StandardBasicTypes.STRING).addScalar("materielCd", StandardBasicTypes.STRING).setResultTransformer(transformer).getResultList();
        query = null;
    } catch (Exception e) {
        log.info(e.getMessage());
    } finally {
        em.close();
    }
    // ?????????
    return qmsOrganizationInfoDTO;
}


@SuppressWarnings({ "unchecked", "deprecation" })
@Override
public List<DropDowmValueDTO> getWorkTeamInfo(){
    EntityManager em = emf.createEntityManager();
    List<DropDowmValueDTO> dropDowmValueDTO = new ArrayList<DropDowmValueDTO>();
    try {
        Query query = em.createNativeQuery("SELECT qoi.organization_cd as \"value\" ,qoi.organization_name as \"label\" FROM qms_organization_info qoi where qoi.flag_status = '0' AND qoi.attribute ='4' ");
        ResultTransformer transformer = Transformers.aliasToBean(DropDowmValueDTO.class);
        dropDowmValueDTO = query.unwrap(org.hibernate.query.NativeQuery.class).addScalar("label", StandardBasicTypes.STRING).addScalar("value", StandardBasicTypes.STRING).setResultTransformer(transformer).getResultList();
        query = null;
    } catch (Exception e) {
        // ????????????
        log.info(e.getMessage());
    } finally {
        em.close();
    }
    // ?????????
    return dropDowmValueDTO;
}


@Transactional
@Override
public Integer updateDefaultProcessInfo(Integer hiddenRightMaterielId,String technologyCd){
    Integer backInfo = 0;
    try {
        // ???????????????????????????????????????0
        Integer updateMaterielId = qmsBomTechnologyRepository.updatehiddenRightMaterielId(hiddenRightMaterielId);
        if (updateMaterielId == 0) {
            backInfo = 2;
            throw new RuntimeException("????????????????????????");
        } else {
            backInfo = 1;
        }
        // ????????????????????????????????????????????????1
        Integer updateMaterielIdAndtechnologyCd = qmsBomTechnologyRepository.updateMaterielIdTechnologyCd(hiddenRightMaterielId, technologyCd);
        if (updateMaterielIdAndtechnologyCd == 0) {
            backInfo = 3;
            throw new RuntimeException("????????????????????????");
        } else {
            backInfo = 0;
        }
    } catch (Exception e) {
        // ????????????
        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        log.info(e.getMessage());
    }
    // ?????????
    return backInfo;
}


@SuppressWarnings({ "unchecked", "deprecation" })
@Override
public List<DropDowmValueDTO> getSubordinateUnitsInfo(){
    EntityManager em = emf.createEntityManager();
    List<DropDowmValueDTO> dropDowmValueDTO = new ArrayList<DropDowmValueDTO>();
    try {
        Query query = em.createNativeQuery("SELECT qoi.organization_cd as \"value\" ,qoi.organization_name as \"label\" FROM qms_organization_info qoi where qoi.flag_status = '0' AND qoi.attribute ='2' ");
        ResultTransformer transformer = Transformers.aliasToBean(DropDowmValueDTO.class);
        dropDowmValueDTO = query.unwrap(org.hibernate.query.NativeQuery.class).addScalar("label", StandardBasicTypes.STRING).addScalar("value", StandardBasicTypes.STRING).setResultTransformer(transformer).getResultList();
        query = null;
    } catch (Exception e) {
        // ????????????
        log.info(e.getMessage());
    } finally {
        em.close();
    }
    // ?????????
    return dropDowmValueDTO;
}


public String getSelectParentNodeInfo(String materielID,String vehicleId){
    StringBuffer objSqlContent = new StringBuffer();
    objSqlContent.append(" SELECT t01.id as \"id\" , ");
    objSqlContent.append(" t01.parent_materiel_id as \"parentCd\" , ");
    objSqlContent.append(" t01.materiel_id as \"organizationCd\" , ");
    objSqlContent.append(" t01.vehicle_id as \"vehicleType\" , ");
    objSqlContent.append(" qm.materiel_name as \"organizationName\",  ");
    objSqlContent.append(" qm.materiel_cd as \"materielCd\"  ");
    objSqlContent.append(" FROM qms_bom t01 ");
    objSqlContent.append(" left join  qms_materiel qm ON qm.id = t01.materiel_id");
    objSqlContent.append(" where 1=1 ");
    // ????????????????????????????????????????????????
    if ("".equals(materielID) && "".equals(vehicleId)) {
        objSqlContent.append(" and t01.parent_materiel_id = '0' or t01.parent_materiel_id = '' or t01.parent_materiel_id is null ");
    }
    // ??????????????????????????????
    if (!"".equals(materielID)) {
        objSqlContent.append(" and t01.materiel_id  like :materielID ");
    }
    // ??????????????????????????????
    if (!"".equals(vehicleId)) {
        objSqlContent.append(" and t01.vehicle_id like :vehicleId ");
    }
    // ????????????????????????????????????????????????
    if (!"".equals(materielID) || !"".equals(vehicleId)) {
        objSqlContent.append("and NOT FIND_IN_SET(t01.parent_materiel_id,(select  ");
        objSqlContent.append(" GROUP_CONCAT(t02.materiel_id)  ");
        objSqlContent.append(" FROM qms_bom t02  ");
        objSqlContent.append(" left join  qms_materiel qm ON qm.id = t02.materiel_id");
        objSqlContent.append(" WHERE 1=1  ");
        // ??????????????????????????????
        if (!"".equals(materielID)) {
            objSqlContent.append(" and t02.materiel_id  like :materielID ");
        }
        // ??????????????????????????????
        if (!"".equals(vehicleId)) {
            objSqlContent.append(" and t02.vehicle_id like :vehicleId ");
        }
        objSqlContent.append("  ))  ");
    }
    // ?????????
    return objSqlContent.toString();
}


public String getBomTechnologyIdAllInfo(String FlagStatus,Integer BomTechnologyId){
    StringBuffer objSqlContent = new StringBuffer();
    objSqlContent.append(" SELECT qpar.id as \"id\" , ");
    objSqlContent.append(" qpar.bom_technology_id as \"bomTechnologyId\" , ");
    objSqlContent.append(" qpar.assembly_num as \"assemblyNum\" , ");
    objSqlContent.append(" qpar.assembly_materiel_id as \"assemblyMaterielId\" , ");
    objSqlContent.append(" qm.materiel_cd as \"assemblyMaterielCd\" , ");
    objSqlContent.append(" qm.materiel_name as \"assemblyMaterielName\" , ");
    objSqlContent.append(" qpar.assembly_count as \"assemblyCount\" , ");
    objSqlContent.append(" qpar.remark as \"remark\" , ");
    objSqlContent.append(" qpar.flag_status as \"flagStatus\" , ");
    objSqlContent.append(" qpar.comp_pkid as \"compPkid\" , ");
    objSqlContent.append(" qpar.reserve_first as \"reserveFirst\" , ");
    objSqlContent.append(" qpar.reserve_second as \"reserveSecond\" , ");
    objSqlContent.append(" qpar.reserve_third as \"reserveThird\" , ");
    objSqlContent.append(" qpar.make_user as \"makeUser\" , ");
    objSqlContent.append(" qpar.modify_user as \"modifyUser\" ");
    objSqlContent.append(" FROM qms_parts_assembly_relation qpar ");
    objSqlContent.append(" LEFT JOIN qms_materiel qm ON qm.id = qpar.assembly_materiel_id and qm.flag_status = 0 ");
    objSqlContent.append(" WHERE 1=1");
    objSqlContent.append(" AND qpar.bom_technology_id = :BomTechnologyId ");
    objSqlContent.append(" AND qpar.flag_status = :FlagStatus ");
    // ?????????
    return objSqlContent.toString();
}


@SuppressWarnings({ "unchecked", "deprecation" })
@Override
public List<ProcessInfomationsRightListDTO> getAllInfoNumber(HashMap<String,Object> param){
    List<ProcessInfomationsRightListDTO> processInfomationsRightList = new ArrayList<ProcessInfomationsRightListDTO>();
    // ????????????????????????
    String materielCdVague = param.get("materielCd").toString();
    // ????????????????????????
    String technologyCd = param.get("technologyCd").toString();
    // ??????????????????
    EntityManager em = emf.createEntityManager();
    try {
        // ??????
        Query query = em.createNativeQuery(getSelectNumberAllInfo(param));
        ResultTransformer transformer = Transformers.aliasToBean(ProcessInfomationsRightListDTO.class);
        query.setParameter("materielCdVague", materielCdVague);
        query.setParameter("technologyCd", technologyCd);
        processInfomationsRightList = query.unwrap(org.hibernate.query.NativeQuery.class).addScalar("numberCount", StandardBasicTypes.STRING).setResultTransformer(transformer).getResultList();
        query = null;
    } catch (Exception e) {
        // ????????????
        log.info(e.getMessage());
    } finally {
        em.close();
    }
    return processInfomationsRightList;
}


public String getSelectNumberAllInfo(HashMap<String,Object> param){
    StringBuffer objSqlContent = new StringBuffer();
    objSqlContent.append(" SELECT count(qbt.id) as \"numberCount\" ");
    objSqlContent.append(" FROM qms_bom_technology qbt ");
    objSqlContent.append(" LEFT JOIN qms_process qp ON qp.id = qbt.process_id AND qp.flag_status = 0");
    objSqlContent.append(" WHERE qbt.flag_status = 0 ");
    objSqlContent.append(" AND qbt.materiel_id = :materielCdVague ");
    objSqlContent.append(" AND qbt.technology_cd = :technologyCd ");
    objSqlContent.append(" ORDER BY qbt.order_no DESC ");
    // ?????????
    return objSqlContent.toString();
}


@Override
@Transactional
public HashMap<String,Object> createCopyProcessInfo(Integer hiddenRightMaterielId,String technologyCd,String technologyName,String copyTechnologyCd){
    HashMap<String, Object> backResult = new HashMap<String, Object>();
    // ????????????????????????????????????
    List<QmsBomTechnology> copyQmsBomTechnology = new ArrayList<QmsBomTechnology>();
    // ????????????????????????????????????????????????
    List<QmsQualityControlDetails> copyQmsQualityControlDetailsList = new ArrayList<QmsQualityControlDetails>();
    // ???????????????????????????????????????
    List<QmsPartsAssemblyRelation> copyQmsPartsAssemblyRelation = new ArrayList<QmsPartsAssemblyRelation>();
    // session??????????????????
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    // ??????????????????
    UserDetails user = (UserDetails) authentication.getPrincipal();
    // ??????????????????
    ZonedDateTime nowTime = dateUtil.getDBNowDate();
    String backString = "";
    String backStringOne = "";
    String backStringOrderNo = "";
    try {
        // ?????????????????????????????????
        copyQmsBomTechnology = qmsBomTechnologyRepository.findByMaterielIdAndTechnologyCdAndFlagStatus(hiddenRightMaterielId, copyTechnologyCd, "0");
        // ???????????????????????????
        if (copyQmsBomTechnology.size() != 0) {
            for (int o = 0; o < copyQmsBomTechnology.size(); o++) {
                // ????????????
                // ?????????????????????????????????
                QmsBomTechnology saveBackQmsBomTechnology = new QmsBomTechnology();
                // ??????id
                saveBackQmsBomTechnology.setMaterielId(copyQmsBomTechnology.get(o).getMaterielId());
                // ????????????
                saveBackQmsBomTechnology.setTechnologyCd(technologyCd);
                // ????????????
                saveBackQmsBomTechnology.setTechnologyName(technologyName);
                // ??????
                saveBackQmsBomTechnology.setOrderNo(copyQmsBomTechnology.get(o).getOrderNo());
                // ?????????id
                saveBackQmsBomTechnology.setBeforeProcessId(copyQmsBomTechnology.get(o).getBeforeProcessId());
                // ??????id
                saveBackQmsBomTechnology.setProcessId(copyQmsBomTechnology.get(o).getProcessId());
                // ????????????
                saveBackQmsBomTechnology.setOrganizationCd(copyQmsBomTechnology.get(o).getOrganizationCd());
                // ????????????
                saveBackQmsBomTechnology.setSchedulerRole(copyQmsBomTechnology.get(o).getSchedulerRole());
                // ??????????????????
                saveBackQmsBomTechnology.setWorkUnit(copyQmsBomTechnology.get(o).getWorkUnit());
                // ????????????
                saveBackQmsBomTechnology.setWorkHours(copyQmsBomTechnology.get(o).getWorkHours());
                // ????????????
                saveBackQmsBomTechnology.setQcType(copyQmsBomTechnology.get(o).getQcType());
                // ??????????????????
                saveBackQmsBomTechnology.setSpecialRole(copyQmsBomTechnology.get(o).getSpecialRole());
                // ????????????
                saveBackQmsBomTechnology.setOperationType(copyQmsBomTechnology.get(o).getOperationType());
                // ??????????????????
                saveBackQmsBomTechnology.setMutualinRole(copyQmsBomTechnology.get(o).getMutualinRole());
                // ????????????????????????
                saveBackQmsBomTechnology.setIsNewCd(copyQmsBomTechnology.get(o).getIsNewCd());
                // ??????????????????ID
                saveBackQmsBomTechnology.setNewCdMaterielId(copyQmsBomTechnology.get(o).getNewCdMaterielId());
                // ????????????
                saveBackQmsBomTechnology.setWorkGroupCd(copyQmsBomTechnology.get(o).getWorkGroupCd());
                // ??????????????????
                saveBackQmsBomTechnology.setIsControlPoint(copyQmsBomTechnology.get(o).getIsControlPoint());
                // ??????????????????
                saveBackQmsBomTechnology.setIsTest(copyQmsBomTechnology.get(o).getIsTest());
                // ????????????
                saveBackQmsBomTechnology.setIsDefault("0");
                // ??????
                saveBackQmsBomTechnology.setRemark(copyQmsBomTechnology.get(o).getRemark());
                // ????????????
                saveBackQmsBomTechnology.setFlagStatus("0");
                saveBackQmsBomTechnology.setMakeTime(nowTime);
                saveBackQmsBomTechnology.setMakeUser(user.getUsername());
                saveBackQmsBomTechnology.setModifyTime(nowTime);
                saveBackQmsBomTechnology.setModifyUser(user.getUsername());
                // ????????????????????????
                saveBackQmsBomTechnology = qmsBomTechnologyRepository.save(saveBackQmsBomTechnology);
                // ??????????????????id??????????????????????????????????????????
                copyQmsQualityControlDetailsList = qmsQualityControlDetailsRepository.findByBomTechnologyIdAndFlagStatus(Integer.valueOf(copyQmsBomTechnology.get(o).getId().toString()), "0");
                // ??????????????????????????????????????????????????????
                for (int c = 0; c < copyQmsQualityControlDetailsList.size(); c++) {
                    // ????????????????????????????????????????????????
                    QmsQualityControlDetails saveQmsQualityControlDetails = new QmsQualityControlDetails();
                    saveQmsQualityControlDetails.setBomTechnologyId(Integer.valueOf(saveBackQmsBomTechnology.getId().toString()));
                    saveQmsQualityControlDetails.setInspectionItem(copyQmsQualityControlDetailsList.get(c).getInspectionItem());
                    saveQmsQualityControlDetails.setTechnicalRequirement(copyQmsQualityControlDetailsList.get(c).getTechnicalRequirement());
                    saveQmsQualityControlDetails.setInspectionInstrument(copyQmsQualityControlDetailsList.get(c).getInspectionInstrument());
                    saveQmsQualityControlDetails.setPlaceDiff(copyQmsQualityControlDetailsList.get(c).getPlaceDiff());
                    saveQmsQualityControlDetails.setStandard(copyQmsQualityControlDetailsList.get(c).getStandard());
                    saveQmsQualityControlDetails.setUpperDeviation(copyQmsQualityControlDetailsList.get(c).getUpperDeviation());
                    saveQmsQualityControlDetails.setLowerDeviation(copyQmsQualityControlDetailsList.get(c).getLowerDeviation());
                    saveQmsQualityControlDetails.setInspectionResultDiff(copyQmsQualityControlDetailsList.get(c).getInspectionResultDiff());
                    saveQmsQualityControlDetails.setIsCheckObj(copyQmsQualityControlDetailsList.get(c).getIsCheckObj());
                    saveQmsQualityControlDetails.setAbcType(copyQmsQualityControlDetailsList.get(c).getAbcType());
                    saveQmsQualityControlDetails.setRemark(copyQmsQualityControlDetailsList.get(c).getRemark());
                    saveQmsQualityControlDetails.setMakeTime(nowTime);
                    saveQmsQualityControlDetails.setMakeUser(user.getUsername());
                    saveQmsQualityControlDetails.setModifyTime(nowTime);
                    saveQmsQualityControlDetails.setModifyUser(user.getUsername());
                    saveQmsQualityControlDetails.setFlagStatus("0");
                    saveQmsQualityControlDetails = qmsQualityControlDetailsRepository.save(saveQmsQualityControlDetails);
                    log.info("????????????????????????????????????????????????" + saveQmsQualityControlDetails);
                }
                // ??????????????????id?????????????????????????????????
                copyQmsPartsAssemblyRelation = qmsPartsAssemblyRelationRepository.findAllByFlagStatusAndBomTechnologyId("0", Integer.valueOf(copyQmsBomTechnology.get(o).getId().toString()));
                // ????????????
                for (int a = 0; a < copyQmsPartsAssemblyRelation.size(); a++) {
                    QmsPartsAssemblyRelation saveQmsPartsAssemblyRelation = new QmsPartsAssemblyRelation();
                    saveQmsPartsAssemblyRelation.setBomTechnologyId(Integer.valueOf(saveBackQmsBomTechnology.getId().toString()));
                    saveQmsPartsAssemblyRelation.setAssemblyNum(copyQmsPartsAssemblyRelation.get(a).getAssemblyNum());
                    saveQmsPartsAssemblyRelation.setAssemblyMaterielId(copyQmsPartsAssemblyRelation.get(a).getAssemblyMaterielId());
                    saveQmsPartsAssemblyRelation.setAssemblyCount(copyQmsPartsAssemblyRelation.get(a).getAssemblyCount());
                    saveQmsPartsAssemblyRelation.setRemark(copyQmsPartsAssemblyRelation.get(a).getRemark());
                    saveQmsPartsAssemblyRelation.setFlagStatus("0");
                    saveQmsPartsAssemblyRelation.setMakeTime(nowTime);
                    saveQmsPartsAssemblyRelation.setMakeUser(user.getUsername());
                    saveQmsPartsAssemblyRelation.setModifyTime(nowTime);
                    saveQmsPartsAssemblyRelation.setModifyUser(user.getUsername());
                    saveQmsPartsAssemblyRelation = qmsPartsAssemblyRelationRepository.save(saveQmsPartsAssemblyRelation);
                    log.info("??????????????????id?????????????????????????????????" + saveQmsPartsAssemblyRelation);
                }
                QmsProcess qmsprocessCd = new QmsProcess();
                qmsprocessCd = qmsProcessRepository.findByFlagStatusAndId("0", Long.valueOf(copyQmsBomTechnology.get(o).getProcessId()));
                if (o == 0) {
                    backString = saveBackQmsBomTechnology.getId().toString();
                    backStringOne = copyQmsBomTechnology.get(o).getId().toString();
                    backStringOrderNo = qmsprocessCd.getProcessCd();
                } else {
                    backString = backString + "," + saveBackQmsBomTechnology.getId().toString();
                    backStringOne = backStringOne + "," + copyQmsBomTechnology.get(o).getId().toString();
                    backStringOrderNo = backStringOrderNo + "," + qmsprocessCd.getProcessCd();
                }
            }
        }
        backResult.put("success", backString);
        backResult.put("successOne", backStringOne);
        backResult.put("backStringOrderNo", backStringOrderNo);
    } catch (Exception e) {
        backResult.put("error", "????????????");
        // ????????????
        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
    }
    // ?????????
    return backResult;
}


@SuppressWarnings({ "unchecked", "deprecation" })
@Override
public List<DropDowmValueDTO> getCarTypeInfo(){
    EntityManager em = emf.createEntityManager();
    List<DropDowmValueDTO> dropDowmValueDTO = new ArrayList<DropDowmValueDTO>();
    try {
        Query query = em.createNativeQuery("SELECT r.role_code as \"value\" ,r.role_name as \"label\" FROM rbac_role r where r.del_flag = 0");
        ResultTransformer transformer = Transformers.aliasToBean(DropDowmValueDTO.class);
        dropDowmValueDTO = query.unwrap(org.hibernate.query.NativeQuery.class).addScalar("label", StandardBasicTypes.STRING).addScalar("value", StandardBasicTypes.STRING).setResultTransformer(transformer).getResultList();
        query = null;
    } catch (Exception e) {
        // ????????????
        log.info(e.getMessage());
    } finally {
        em.close();
    }
    // ?????????
    return dropDowmValueDTO;
}


@SuppressWarnings({ "deprecation", "unchecked" })
@Override
public List<DropDowmValueDTO> getAllTechnology(String masterCd){
    EntityManager em = emf.createEntityManager();
    List<DropDowmValueDTO> dropDowmValueDTO = new ArrayList<DropDowmValueDTO>();
    try {
        Query query = em.createNativeQuery("SELECT DISTINCT qbt.technology_cd as \"value\" ,qbt.technology_name as \"label\"  FROM qms_bom_technology  qbt WHERE qbt.materiel_id = '" + masterCd + "' AND qbt.flag_status = '0' ORDER BY qbt.is_default desc  ");
        ResultTransformer transformer = Transformers.aliasToBean(DropDowmValueDTO.class);
        dropDowmValueDTO = query.unwrap(org.hibernate.query.NativeQuery.class).addScalar("label", StandardBasicTypes.STRING).addScalar("value", StandardBasicTypes.STRING).setResultTransformer(transformer).getResultList();
        query = null;
    } catch (Exception e) {
        // ????????????
        log.info(e.getMessage());
    } finally {
        em.close();
    }
    // ?????????
    return dropDowmValueDTO;
}


@Transactional
@Override
public Integer createInfo(BomTechnologGroupDTO bomTechnologGroupDTO){
    // ????????????
    Integer backResult = 0;
    log.debug("???????????????????????????" + bomTechnologGroupDTO);
    log.debug("???????????????????????????" + bomTechnologGroupDTO.getQmsBomTechnology());
    log.debug("???????????????????????????" + bomTechnologGroupDTO.getQmsEnclosure());
    log.debug("???????????????????????????" + bomTechnologGroupDTO.getQmsPartsAssemblyRelation());
    log.debug("???????????????????????????" + bomTechnologGroupDTO.getQmsQualityControlDetails());
    // session??????????????????
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    // ??????????????????
    UserDetails user = (UserDetails) authentication.getPrincipal();
    // ??????????????????
    ZonedDateTime nowTime = dateUtil.getDBNowDate();
    try {
        // ?????????????????????????????????
        List<QmsBomTechnology> existenceCheckQBT = qmsBomTechnologyRepository.findByMaterielIdAndTechnologyCdAndOrderNoAndFlagStatus(bomTechnologGroupDTO.getQmsBomTechnology().getMaterielId(), bomTechnologGroupDTO.getQmsBomTechnology().getTechnologyCd(), bomTechnologGroupDTO.getQmsBomTechnology().getOrderNo(), "0");
        // ??????????????????????????????????????????
        if (existenceCheckQBT.size() != 0) {
            // ???????????? ??????????????????????????????
            backResult = 1;
            return backResult;
        }
        // ????????????ID??????????????????
        List<QmsBomTechnology> existenceCheckIsDefault = qmsBomTechnologyRepository.findByMaterielIdAndIsDefaultAndFlagStatus(bomTechnologGroupDTO.getQmsBomTechnology().getMaterielId(), "1", "0");
        // ????????????????????????????????????
        if (existenceCheckIsDefault.size() == 0) {
            // ???????????????????????????1
            bomTechnologGroupDTO.getQmsBomTechnology().setIsDefault("1");
        } else {
            List<QmsBomTechnology> gyAndwl = qmsBomTechnologyRepository.findByMaterielIdAndTechnologyCdAndIsDefaultAndFlagStatus(bomTechnologGroupDTO.getQmsBomTechnology().getMaterielId(), bomTechnologGroupDTO.getQmsBomTechnology().getTechnologyCd(), "1", "0");
            if (gyAndwl.size() == 0) {
                // ???????????????????????????0
                bomTechnologGroupDTO.getQmsBomTechnology().setIsDefault("0");
            } else {
                // ???????????????????????????0
                bomTechnologGroupDTO.getQmsBomTechnology().setIsDefault("1");
            }
        }
        // ????????????
        bomTechnologGroupDTO.getQmsBomTechnology().setMakeTime(nowTime);
        // ?????????
        bomTechnologGroupDTO.getQmsBomTechnology().setMakeUser(user.getUsername());
        // ????????????
        bomTechnologGroupDTO.getQmsBomTechnology().setModifyTime(nowTime);
        // ?????????
        bomTechnologGroupDTO.getQmsBomTechnology().setModifyUser(user.getUsername());
        // ????????????
        bomTechnologGroupDTO.getQmsBomTechnology().setFlagStatus("0");
        // ?????????????????????
        QmsBomTechnology insertBackInfo = qmsBomTechnologyRepository.save(bomTechnologGroupDTO.getQmsBomTechnology());
        // ?????????????????????????????????????????????????????????????????????
        for (int a = 0; a < bomTechnologGroupDTO.getQmsQualityControlDetails().size(); a++) {
            // ??????????????????????????? ???1???0
            if (null != bomTechnologGroupDTO.getQmsQualityControlDetails().get(a).getStandard()) {
                bomTechnologGroupDTO.getQmsQualityControlDetails().get(a).setInspectionResultDiff("0");
            } else {
                bomTechnologGroupDTO.getQmsQualityControlDetails().get(a).setInspectionResultDiff("1");
            }
            // ??????ID
            bomTechnologGroupDTO.getQmsQualityControlDetails().get(a).setBomTechnologyId(Integer.valueOf(insertBackInfo.getId().toString()));
            // ????????????
            bomTechnologGroupDTO.getQmsQualityControlDetails().get(a).setMakeTime(nowTime);
            // ?????????
            bomTechnologGroupDTO.getQmsQualityControlDetails().get(a).setMakeUser(user.getUsername());
            // ????????????
            bomTechnologGroupDTO.getQmsQualityControlDetails().get(a).setModifyTime(nowTime);
            // ?????????
            bomTechnologGroupDTO.getQmsQualityControlDetails().get(a).setModifyUser(user.getUsername());
            // ????????????
            bomTechnologGroupDTO.getQmsQualityControlDetails().get(a).setFlagStatus("0");
        }
        // ?????????????????????????????????
        qmsQualityControlDetailsRepository.saveAll(bomTechnologGroupDTO.getQmsQualityControlDetails());
        List<QmsPartsAssemblyRelation> qmsPartsAssemblyRelationList = new ArrayList<QmsPartsAssemblyRelation>();
        // ??????????????????????????????????????????????????????
        for (int b = 0; b < bomTechnologGroupDTO.getQmsPartsAssemblyRelation().size(); b++) {
            QmsPartsAssemblyRelation partsAssemblyRelation = new QmsPartsAssemblyRelation();
            // ??????ID
            partsAssemblyRelation.setBomTechnologyId(bomTechnologGroupDTO.getQmsPartsAssemblyRelation().get(b).getBomTechnologyId());
            // ????????????
            partsAssemblyRelation.setAssemblyNum(bomTechnologGroupDTO.getQmsPartsAssemblyRelation().get(b).getAssemblyNum());
            // ????????????ID
            partsAssemblyRelation.setAssemblyMaterielId(bomTechnologGroupDTO.getQmsPartsAssemblyRelation().get(b).getAssemblyMaterielId());
            // ??????
            partsAssemblyRelation.setAssemblyCount(bomTechnologGroupDTO.getQmsPartsAssemblyRelation().get(b).getAssemblyCount());
            // ??????
            partsAssemblyRelation.setRemark(bomTechnologGroupDTO.getQmsPartsAssemblyRelation().get(b).getRemark());
            // ??????ID
            partsAssemblyRelation.setBomTechnologyId(Integer.valueOf(insertBackInfo.getId().toString()));
            // ????????????
            partsAssemblyRelation.setMakeTime(nowTime);
            // ?????????
            partsAssemblyRelation.setMakeUser(user.getUsername());
            // ????????????
            partsAssemblyRelation.setModifyTime(nowTime);
            // ?????????
            partsAssemblyRelation.setModifyUser(user.getUsername());
            // ????????????
            partsAssemblyRelation.setFlagStatus("0");
            qmsPartsAssemblyRelationList.add(partsAssemblyRelation);
        }
        // ??????????????????
        qmsPartsAssemblyRelationRepository.saveAll(qmsPartsAssemblyRelationList);
        // // ???????????????????????????????????????????????????
        // for(int c = 0;c<bomTechnologGroupDTO.getQmsEnclosure().size();c++){
        // 
        // // ????????????
        // bomTechnologGroupDTO.getQmsEnclosure().get(c).setMakeTime(nowTime);
        // // ?????????
        // bomTechnologGroupDTO.getQmsEnclosure().get(c).setMakeUser(user.getUsername());
        // // ????????????
        // bomTechnologGroupDTO.getQmsEnclosure().get(c).setModifyTime(nowTime);
        // // ?????????
        // bomTechnologGroupDTO.getQmsEnclosure().get(c).setModifyUser(user.getUsername());
        // }
        // // ???????????????
        // qmsEnclosureRepository.saveAll(bomTechnologGroupDTO.getQmsEnclosure());
        // ????????????????????????ID
        backResult = Integer.valueOf(insertBackInfo.getId().toString());
    } catch (Exception e) {
        // ????????????
        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        // ????????????
        log.info(e.getMessage());
        // ???????????? ????????????
        backResult = 0;
        return backResult;
    }
    // ?????????
    return backResult;
}


@Override
@Transactional
public Boolean uploadEnclosure(Integer inspectionInfoId,String inspectionKbn,String enclosureAddress){
    // session??????????????????
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    // ??????????????????
    UserDetails user = (UserDetails) authentication.getPrincipal();
    // ??????????????????
    ZonedDateTime nowTime = dateUtil.getDBNowDate();
    QmsEnclosure qmsEnclosure = new QmsEnclosure();
    try {
        qmsEnclosure.setEnclosureAddress(enclosureAddress);
        qmsEnclosure.setInspectionInfoId(inspectionInfoId);
        qmsEnclosure.setInspectionKbn(inspectionKbn);
        qmsEnclosure.setMakeTime(nowTime);
        qmsEnclosure.setMakeUser(user.getUsername());
        qmsEnclosure.setModifyTime(nowTime);
        qmsEnclosure.setModifyUser(user.getUsername());
        qmsEnclosure = qmsEnclosureRepository.save(qmsEnclosure);
        // ????????????????????????
        if (null == qmsEnclosure) {
            return false;
        }
    } catch (Exception e) {
        // ????????????
        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        // ????????????
        log.info(e.getMessage());
    }
    return true;
}


}