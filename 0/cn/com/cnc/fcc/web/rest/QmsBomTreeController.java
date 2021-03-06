import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.alibaba.fastjson.JSONObject;
import com.codahale.metrics.annotation.Timed;
import cn.com.cnc.fcc.domain.QmsBom;
import cn.com.cnc.fcc.domain.QmsMateriel;
import cn.com.cnc.fcc.domain.QmsVehicleTypeInfo;
import cn.com.cnc.fcc.repository.QmsBomRepository;
import cn.com.cnc.fcc.repository.QmsMaterielRepository;
import cn.com.cnc.fcc.repository.QmsVehicleTypeInfoRepository;
import cn.com.cnc.fcc.service.QmsBomTreeService;
import cn.com.cnc.fcc.service.dto.DropDowmValueDTO;
import cn.com.cnc.fcc.service.dto.QmsBomDetaileDTO;
import cn.com.cnc.fcc.service.dto.QmsOrganizationInfoDTO;
import cn.com.cnc.fcc.service.dto.QmsOrganizationInfoLeftDTO;
import cn.com.cnc.fcc.service.util.DateUtil;
import cn.com.cnc.fcc.service.util.TreeCommon;
import cn.com.cnc.fcc.web.rest.errors.BadRequestAlertException;
import cn.com.cnc.fcc.web.rest.util.HeaderUtil;
import io.github.jhipster.web.util.ResponseUtil;
@RestController
@RequestMapping("/api")
public class QmsBomTreeController {

 private  Logger log;

@Autowired
 private  QmsBomTreeService qmsBomTreeService;

@Resource
 private  DateUtil dateUtil;

@Autowired
 private  TreeCommon treeCommon;

 private  String ENTITY_NAME;

 private  QmsBomRepository qmsBomRepository;

 private  QmsMaterielRepository qmsMaterielRepository;

 private  QmsVehicleTypeInfoRepository qmsVehicleTypeInfoRepository;


@PutMapping("/qms-bom-infos/UpdateInfo")
@Timed
public ResponseEntity<QmsBom> updateOrganizationInfo(QmsBom qmsOrganizationInfo){
    log.debug("REST request to update QmsOrganizationInfo : {}", qmsOrganizationInfo);
    if (qmsOrganizationInfo.getId() == null) {
        throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
    }
    // ??????????????????
    List<QmsBom> organizationInfo = new ArrayList<QmsBom>();
    // ??????????????????????????????
    organizationInfo = qmsBomRepository.findByVehicleIdAndParentMaterielIDAndMaterielIdAndFlagStatusAndIdNot(qmsOrganizationInfo.getVehicleId(), qmsOrganizationInfo.getParentMaterielID(), qmsOrganizationInfo.getMaterielId(), "0", qmsOrganizationInfo.getId());
    // ??????????????????????????????
    if (organizationInfo.size() > 0) {
        // ??????????????????????????????
        throw new BadRequestAlertException("codingDuplication", ENTITY_NAME, "idexists");
    }
    // session??????????????????
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    // ??????????????????
    UserDetails user = (UserDetails) authentication.getPrincipal();
    // ????????????????????????0
    qmsOrganizationInfo.setFlagStatus("0");
    // ??????????????????
    qmsOrganizationInfo.setModifyTime(dateUtil.getDBNowDate());
    // ???????????????
    qmsOrganizationInfo.setModifyUser(user.getUsername());
    QmsBom result = qmsBomRepository.save(qmsOrganizationInfo);
    return ResponseEntity.ok().headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, qmsOrganizationInfo.getId().toString())).body(result);
}


@RequestMapping("/qms-bom-infos/getAllList")
public HashMap<String,Object> getDictionaryTess(HashMap<String,Object> param){
    HashMap<String, Object> data = new HashMap<String, Object>();
    // ??????????????????????????????
    List<QmsOrganizationInfoLeftDTO> qmsOrganizationInfoDTO = new ArrayList<QmsOrganizationInfoLeftDTO>();
    // ???????????????????????????
    List<QmsOrganizationInfoDTO> qmsOrganizationInfoDTOBack = new ArrayList<QmsOrganizationInfoDTO>();
    // ???????????????????????????
    List<QmsOrganizationInfoDTO> parentNodeList = new ArrayList<QmsOrganizationInfoDTO>();
    // ????????????
    String materielCd = param.get("organizationCd").toString();
    // ????????????
    String materielName = param.get("organizationName").toString();
    // ?????????????????????
    parentNodeList = qmsBomTreeService.getParentNodeListInfo(materielCd, materielName);
    // ??????????????????
    qmsOrganizationInfoDTOBack = qmsBomTreeService.organListInfo();
    if (qmsOrganizationInfoDTOBack.size() > 0) {
        qmsOrganizationInfoDTO = treeCommon.TreeStructureUtilBom(qmsOrganizationInfoDTOBack, parentNodeList);
    }
    data.put("qmsOrganization", qmsOrganizationInfoDTO);
    return data;
}


@GetMapping("/qms-bom-infos/deleteInfo")
public Integer deleteOrganizationTypeInfo(HttpServletRequest request){
    String id = request.getParameter("materielIdInfo");
    String hidVehicleType = request.getParameter("hidVehicleType");
    // ?????????????????????
    Integer resultNumber = 0;
    try {
        // ????????????
        resultNumber = qmsBomTreeService.deleteNodeInfos(id, hidVehicleType);
    } catch (Exception e) {
        // ????????????
        log.info(e.getMessage());
    }
    // ????????????
    return resultNumber;
}


@PostMapping("/qms-bom-infos/CreateInfo")
@Timed
public ResponseEntity<QmsBom> createOrganizationInfo(QmsBom qmsOrganizationInfo){
    log.debug("REST request to save QmsOrganizationInfo : {}", qmsOrganizationInfo);
    if (qmsOrganizationInfo.getId() != null) {
        throw new BadRequestAlertException("A new qmsOrganizationInfo cannot already have an ID", ENTITY_NAME, "idexists");
    }
    // ??????????????????
    List<QmsBom> organizationInfo = new ArrayList<QmsBom>();
    // session??????????????????
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    // ??????????????????
    UserDetails user = (UserDetails) authentication.getPrincipal();
    // ????????????id????????????
    if ("0".equals(qmsOrganizationInfo.getParentMaterielID()) || "".equals(qmsOrganizationInfo.getParentMaterielID()) || null == qmsOrganizationInfo.getParentMaterielID()) {
        // ??????????????????????????????
        organizationInfo = qmsBomRepository.findByVehicleIdAndMaterielIdAndParentMaterielIDAndFlagStatus(qmsOrganizationInfo.getVehicleId(), qmsOrganizationInfo.getMaterielId(), 0, "0");
        // ??????????????????????????????
        if (organizationInfo.size() > 0) {
            // ??????????????????????????????
            throw new BadRequestAlertException("codingDuplication", ENTITY_NAME, "idexists");
        }
    } else {
        // ??????????????????????????????
        organizationInfo = qmsBomRepository.findByVehicleIdAndParentMaterielIDAndMaterielIdAndFlagStatus(qmsOrganizationInfo.getVehicleId(), qmsOrganizationInfo.getParentMaterielID(), qmsOrganizationInfo.getMaterielId(), "0");
        // ??????????????????????????????
        if (organizationInfo.size() > 0) {
            // ??????????????????????????????
            throw new BadRequestAlertException("codingDuplication", ENTITY_NAME, "idexists");
        }
    }
    // ????????????????????????0
    qmsOrganizationInfo.setFlagStatus("0");
    // ???????????????
    qmsOrganizationInfo.setMakeUser(user.getUsername());
    // ??????????????????
    qmsOrganizationInfo.setMakeTime(dateUtil.getDBNowDate());
    // ???????????????
    qmsOrganizationInfo.setModifyUser(user.getUsername());
    // ??????????????????
    qmsOrganizationInfo.setModifyTime(dateUtil.getDBNowDate());
    QmsBom result = qmsBomRepository.save(qmsOrganizationInfo);
    return ResponseEntity.created(new URI("/api/qms-bom-infos/" + result.getId())).headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString())).body(result);
}


@RequestMapping("/qms-bom-infos/upload")
public JSONObject upload(MultipartFile files){
    // ???????????????
    JSONObject returnData = new JSONObject();
    returnData = qmsBomTreeService.uploadUserDepart(files);
    // ??????????????????
    return returnData;
}


@GetMapping("/qms-bom-infos/carType/{id}")
@Timed
public ResponseEntity<QmsVehicleTypeInfo> getQmsVehicleTypeClass(String id){
    log.debug("REST request to get QmsVehicleTypeClass : {}", id);
    Optional<QmsVehicleTypeInfo> qmsVehicleTypeClass = qmsVehicleTypeInfoRepository.findByFlagStatusAndVehicleTypeOrderById("0", id);
    List<QmsVehicleTypeInfo> qmsVehicleTypeClassInfo = qmsVehicleTypeInfoRepository.findByFlagStatusAndVehicleType("0", id);
    if (qmsVehicleTypeClassInfo.size() == 0) {
        qmsVehicleTypeClass = Optional.ofNullable(new QmsVehicleTypeInfo());
    }
    return ResponseUtil.wrapOrNotFound(qmsVehicleTypeClass);
}


@GetMapping("/qms-bom-infos/getAllCarType")
public List<DropDowmValueDTO> getCarTypeInfo(){
    // ???????????????
    List<DropDowmValueDTO> dropDowmValueDTO = new ArrayList<DropDowmValueDTO>();
    // ????????????
    dropDowmValueDTO = qmsBomTreeService.getCarTypeInfo();
    // ???????????????
    return dropDowmValueDTO;
}


@GetMapping("/qms-bom-infos/materielName/{materielCd}")
@Timed
public ResponseEntity<QmsMateriel> getQmsMateriel(String materielCd){
    log.debug("REST request to get QmsMateriel : {}", materielCd);
    // ??????????????????
    List<QmsMateriel> qmsMaterielList = qmsMaterielRepository.findByMaterielCdAndFlagStatus(materielCd, "0");
    // ??????????????????
    QmsMateriel qmsMaterielInfo = new QmsMateriel();
    // ????????????????????????
    if (qmsMaterielList.size() != 0) {
        // ??????
        qmsMaterielInfo = qmsMaterielList.get(0);
    }
    // ??????
    Optional<QmsMateriel> qmsMateriel = Optional.ofNullable(qmsMaterielInfo);
    // ??????
    return ResponseUtil.wrapOrNotFound(qmsMateriel);
}


@GetMapping("/qms-bom-infos/detail/{id}")
@Timed
public ResponseEntity<QmsBomDetaileDTO> getOrganizationInfoss(Long id){
    log.debug("REST request to get QmsOrganizationInfo : {}", id);
    Optional<QmsBom> qmsOrganizationInfo = qmsBomRepository.findById(id);
    Optional<QmsBomDetaileDTO> qmsBomDetaileDTO = Optional.ofNullable(new QmsBomDetaileDTO());
    // ????????????ID??????????????????
    List<QmsMateriel> qmsOrgan = new ArrayList<QmsMateriel>();
    if (null != qmsOrganizationInfo.get().getParentMaterielID() && qmsOrganizationInfo.get().getParentMaterielID() != 0) {
        qmsOrgan = qmsMaterielRepository.findByIdAndFlagStatus(Long.valueOf(qmsOrganizationInfo.get().getParentMaterielID()), "0");
    }
    if (qmsOrgan.size() != 0) {
        // ????????????
        qmsBomDetaileDTO.get().setParentMaterielName(qmsOrgan.get(0).getMaterielName());
    }
    // ????????????ID??????????????????
    List<QmsMateriel> qmsOrgaName = qmsMaterielRepository.findByIdAndFlagStatus(Long.valueOf(qmsOrganizationInfo.get().getMaterielId()), "0");
    if (qmsOrgaName.size() != 0) {
        // ????????????
        qmsBomDetaileDTO.get().setMaterielName(qmsOrgaName.get(0).getMaterielName());
        qmsBomDetaileDTO.get().setmId(qmsOrgaName.get(0).getId());
        qmsBomDetaileDTO.get().setMaterielCd(qmsOrgaName.get(0).getMaterielCd());
    }
    List<QmsVehicleTypeInfo> qmsVehicleTypeInfo = qmsVehicleTypeInfoRepository.findByFlagStatusAndId("0", Long.valueOf(qmsOrganizationInfo.get().getVehicleId()));
    if (qmsVehicleTypeInfo.size() != 0) {
        qmsBomDetaileDTO.get().setVehicleId(qmsVehicleTypeInfo.get(0).getVehicleType());
        qmsBomDetaileDTO.get().setVehicleName(qmsVehicleTypeInfo.get(0).getVehicleTypeName());
    }
    // ????????????
    qmsBomDetaileDTO.get().setRootMaterielId(qmsOrganizationInfo.get().getRootMaterielId());
    qmsBomDetaileDTO.get().setMaterielId(qmsOrganizationInfo.get().getMaterielId());
    qmsBomDetaileDTO.get().setParentMaterielID(qmsOrganizationInfo.get().getParentMaterielID());
    qmsBomDetaileDTO.get().setRemark(qmsOrganizationInfo.get().getRemark());
    qmsBomDetaileDTO.get().setId(qmsOrganizationInfo.get().getId());
    qmsBomDetaileDTO.get().setMakeTime(qmsOrganizationInfo.get().getMakeTime());
    qmsBomDetaileDTO.get().setMakeUser(qmsOrganizationInfo.get().getMakeUser());
    qmsBomDetaileDTO.get().setvId(qmsOrganizationInfo.get().getVehicleId());
    // ?????????
    return ResponseUtil.wrapOrNotFound(qmsBomDetaileDTO);
}


}