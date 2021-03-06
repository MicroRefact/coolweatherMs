import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.hibernate.transform.ResultTransformer;
import org.hibernate.transform.Transformers;
import org.hibernate.type.StandardBasicTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import cn.com.cnc.fcc.domain.RbacRole;
import cn.com.cnc.fcc.domain.RbacRoleRightRelation;
import cn.com.cnc.fcc.repository.RbacRightRepository;
import cn.com.cnc.fcc.repository.RbacRoleRepository;
import cn.com.cnc.fcc.repository.RbacRoleRightRelationRepository;
import cn.com.cnc.fcc.service.RbacRoleService;
import cn.com.cnc.fcc.service.dto.RbacRightDTO;
import cn.com.cnc.fcc.service.dto.RbacRoleDTO;
import cn.com.cnc.fcc.service.util.DateUtil;
import cn.com.cnc.fcc.web.rest.errors.InternalServerErrorException;
@Service
public class RbacRoleServiceImpl implements cn.com.cnc.fcc.service.RbacRoleService,RbacRoleService{

 private  Logger log;

@Resource
 private  RbacRightRepository rbacRightRepository;

@Resource
 private  RbacRoleRepository rbacRoleRepository;

@Resource
 private  RbacRoleRightRelationRepository rbacRoleRightRelationRepository;

@PersistenceContext
 private  EntityManager entityManager;

@Autowired
 private  DateUtil dateUtil;

 private  EntityManagerFactory emf;


public String getMaxRoleCodeSQL(){
    StringBuffer objSqlContent = new StringBuffer();
    objSqlContent.append(" SELECT CASE when b.role_code > 9  THEN CONCAT('OA0',b.role_code) ");
    objSqlContent.append("  ELSE CONCAT('OA00',b.role_code) END  as \"roleCode\" 			");
    objSqlContent.append(" FROM   															");
    objSqlContent.append(" (SELECT (MAX(a.role_code) + 1) as role_code        				");
    objSqlContent.append(" FROM                   											");
    objSqlContent.append(" (SELECT substr(r.role_code,3) as role_code FROM `rbac_role` r    ");
    objSqlContent.append(" WHERE r.del_flag = 0) a) b                 						");
    return objSqlContent.toString();
}


@Override
@Transactional
public Integer updateRole(String selectListVal,RbacRole rbacRole){
    // ??????????????????
    Integer updateRole = 0;
    // ??????
    ZonedDateTime time = dateUtil.getDBNowDate();
    RbacRoleRightRelation rbacRoleRightRelation = new RbacRoleRightRelation();
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    UserDetails user = (UserDetails) authentication.getPrincipal();
    // ?????????
    EntityManager em = emf.createEntityManager();
    try {
        /*??????????????????*/
        Date updatetimeN = Date.from(rbacRole.getUpdDateTime().toInstant());
        /*????????????????????????*/
        Timestamp updatetimeNEW = new Timestamp(updatetimeN.getTime());
        // ??????????????????????????????
        // rbacRolesn = rbacRoleRepository.findById(rbacRole.getId()).get();
        Query query = em.createNativeQuery(getUpdataTimeSQL());
        query.setParameter("Id", rbacRole.getId());
        /*????????????????????????*/
        Timestamp updatetimeBef = (Timestamp) query.getSingleResult();
        // ????????????
        if (!updatetimeNEW.equals(updatetimeBef)) {
            updateRole = 5;
            return updateRole;
        // throw new RuntimeException("????????????");
        }
        // ??????
        if (rbacRole.getDelFlag() == 0) {
            // ?????????
            rbacRole.setUpdOperCd(user.getUsername());
            // ????????????
            rbacRole.setUpdDateTime(time);
            // ????????????
            rbacRole.setUpdProgarmCd("RbacRoleController");
            rbacRole = rbacRoleRepository.save(rbacRole);
            if (null == rbacRole) {
                updateRole = 1;
                throw new RuntimeException("??????????????????");
            }
            int delRelation = rbacRoleRightRelationRepository.deleteByRoleId(Integer.valueOf(rbacRole.getId().toString()));
            if (delRelation == 0) {
                updateRole = 2;
                throw new RuntimeException("??????????????????????????????");
            }
            // ????????????
            rbacRoleRightRelation.setDelFlag(0);
            // ????????????
            rbacRoleRightRelation.setStopFlag(0);
            // ????????????
            rbacRoleRightRelation.setInsDateTime(time);
            // ????????????
            rbacRoleRightRelation.setUpdDateTime(time);
            // ??????id
            rbacRoleRightRelation.setRoleId(Integer.valueOf(rbacRole.getId().toString()));
            // ??????id
            rbacRoleRightRelation.setRightId(Integer.valueOf(selectListVal));
            // ?????????
            rbacRoleRightRelation.setInsOperCd(user.getUsername());
            // ????????????
            rbacRoleRightRelation.setUpdProgarmCd("RbacRoleController");
            // ??????????????????
            rbacRoleRightRelation.setTriggerDateTime(time);
            rbacRoleRightRelation = rbacRoleRightRelationRepository.save(rbacRoleRightRelation);
            if (null == rbacRoleRightRelation) {
                updateRole = 2;
                throw new RuntimeException("????????????????????????");
            }
        }
        if (rbacRole.getDelFlag() == 1) {
            // ????????????
            rbacRole.setDelDateTime(time);
            // ?????????
            rbacRole.setDelOperCd(user.getUsername());
            // ????????????
            rbacRole.setDelProgarmCd("RbacRoleController");
            rbacRole = rbacRoleRepository.save(rbacRole);
            if (null == rbacRole) {
                updateRole = 3;
                throw new RuntimeException("??????????????????(??????)");
            }
            int delRelation = rbacRoleRightRelationRepository.updateByRoleId(Integer.valueOf(rbacRole.getId().toString()));
            if (delRelation == 0) {
                updateRole = 4;
                throw new RuntimeException("??????????????????????????????(??????)");
            }
        }
    } catch (Exception e) {
        // ????????????
        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        log.info("????????????");
    } finally {
        // ??????
        em.close();
    }
    return updateRole;
}


@SuppressWarnings("unchecked")
public List<RbacRoleDTO> getRoleRightInfo(Integer roleId){
    EntityManager em = emf.createEntityManager();
    List<RbacRoleDTO> rbacRole = new ArrayList<RbacRoleDTO>();
    try {
        Query query = em.createNativeQuery(getRoleRightSQL());
        query.setParameter("roleId", roleId);
        rbacRole = query.getResultList();
        query = null;
    } catch (Exception e) {
        throw new InternalServerErrorException("rbacRole could not be found");
    } finally {
        em.close();
    }
    // ?????????
    return rbacRole;
}


@SuppressWarnings("unchecked")
public List<RbacRightDTO> getRightList(){
    EntityManager em = emf.createEntityManager();
    List<RbacRightDTO> rbacRight = new ArrayList<RbacRightDTO>();
    try {
        String sql = "SELECT r.id,r.right_name as rightName FROM rbac_right r where r.del_flag = 0";
        Query query = em.createNativeQuery(sql);
        ResultTransformer transformer = Transformers.aliasToBean(RbacRightDTO.class);
        rbacRight = query.unwrap(org.hibernate.query.NativeQuery.class).addScalar("rightName", StandardBasicTypes.STRING).addScalar("id", StandardBasicTypes.LONG).setResultTransformer(transformer).getResultList();
        query = null;
    } catch (Exception e) {
        throw new InternalServerErrorException("rbacRole could not be found");
    } finally {
        em.close();
    }
    // ?????????
    return rbacRight;
}


@Override
@Transactional
@SuppressWarnings("unchecked")
public Integer createRole(String selectListVal,RbacRole rbacRole){
    // ??????????????????
    Integer createRole = 0;
    // ??????
    ZonedDateTime time = dateUtil.getDBNowDate();
    // ?????????
    RbacRole rbacRoles = new RbacRole();
    RbacRoleRightRelation rbacRoleRightRelation = new RbacRoleRightRelation();
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    UserDetails user = (UserDetails) authentication.getPrincipal();
    EntityManager em = emf.createEntityManager();
    List<String> list = new ArrayList<String>();
    try {
        // ??????????????????
        Query query = em.createNativeQuery(getMaxRoleCodeSQL());
        list = query.getResultList();
        // ??????????????????
        String maxCode = list.get(0);
        // ????????????
        rbacRoles.setDelFlag(0);
        // ????????????
        rbacRoles.setStopFlag(0);
        // ????????????
        rbacRoles.setInsDateTime(time);
        // ????????????
        rbacRoles.setUpdDateTime(time);
        // ??????Code
        rbacRoles.setRoleCode(rbacRole.getRoleCode());
        // ????????????
        rbacRoles.setRoleName(rbacRole.getRoleName());
        // ?????????
        rbacRoles.setInsOperCd(user.getUsername());
        // ????????????
        rbacRoles.setInsProgarmCd("RbacRoleController");
        // ??????????????????
        rbacRoles.setTriggerDateTime(time);
        rbacRoles = rbacRoleRepository.save(rbacRoles);
        if (null == rbacRoles) {
            createRole = 1;
            throw new RuntimeException("??????????????????");
        }
        // ????????????
        rbacRoleRightRelation.setDelFlag(0);
        // ????????????
        rbacRoleRightRelation.setStopFlag(0);
        // ????????????
        rbacRoleRightRelation.setInsDateTime(time);
        // ????????????
        rbacRoleRightRelation.setUpdDateTime(time);
        // ??????id
        rbacRoleRightRelation.setRoleId(Integer.valueOf(rbacRoles.getId().toString()));
        // ??????id
        rbacRoleRightRelation.setRightId(Integer.valueOf(selectListVal));
        // ?????????
        rbacRoleRightRelation.setInsOperCd(user.getUsername());
        // ????????????
        rbacRoleRightRelation.setInsProgarmCd("RbacRoleController");
        // ??????????????????
        rbacRoleRightRelation.setTriggerDateTime(time);
        rbacRoleRightRelation = rbacRoleRightRelationRepository.save(rbacRoleRightRelation);
        if (null == rbacRoleRightRelation) {
            createRole = 2;
            throw new RuntimeException("????????????????????????");
        }
    } catch (Exception e) {
        // ????????????
        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        log.info("????????????");
    } finally {
        // ??????
        em.close();
    }
    return createRole;
}


public String getUpdataTimeSQL(){
    StringBuffer objSqlContent = new StringBuffer();
    objSqlContent.append(" select upd_date_time from rbac_role		  ");
    objSqlContent.append(" where  id = :Id		  ");
    objSqlContent.append(" and del_flag = 0		  ");
    return objSqlContent.toString();
}


public String getRoleRightSQL(){
    StringBuffer objSqlContent = new StringBuffer();
    objSqlContent.append(" select rr.id as \"id\", r.right_name as \"rightName\",");
    objSqlContent.append(" rr.right_id  as \"rightId\", rr.role_id as \"roleId\", ");
    objSqlContent.append(" o.upd_date_time  as \"updateTime\" ");
    objSqlContent.append(" FROM rbac_role_right_relation rr     ");
    objSqlContent.append(" INNER JOIN rbac_right r  ON          ");
    objSqlContent.append(" rr.right_id = r.id                   ");
    objSqlContent.append(" AND rr.del_flag = 0                  ");
    objSqlContent.append(" AND r.del_flag = 0                   ");
    objSqlContent.append(" INNER JOIN rbac_role o ON            ");
    objSqlContent.append(" rr.role_id = o.id     		      	");
    objSqlContent.append(" AND o.del_flag = 0     		      	");
    objSqlContent.append(" where rr.role_id = :roleId           ");
    return objSqlContent.toString();
}


}