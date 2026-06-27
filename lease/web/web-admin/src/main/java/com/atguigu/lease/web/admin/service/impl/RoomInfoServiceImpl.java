package com.atguigu.lease.web.admin.service.impl;

import com.atguigu.lease.model.entity.*;
import com.atguigu.lease.model.enums.ItemType;
import com.atguigu.lease.web.admin.mapper.RoomInfoMapper;
import com.atguigu.lease.web.admin.service.*;
import com.atguigu.lease.web.admin.vo.graph.GraphVo;
import com.atguigu.lease.web.admin.vo.room.RoomItemVo;
import com.atguigu.lease.web.admin.vo.room.RoomQueryVo;
import com.atguigu.lease.web.admin.vo.room.RoomSubmitVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author liubo
 * @description 针对表【room_info(房间信息表)】的数据库操作Service实现
 * @createDate 2023-07-24 15:48:00
 */
@Service
public class RoomInfoServiceImpl extends ServiceImpl<RoomInfoMapper, RoomInfo>
        implements RoomInfoService {

    @Autowired
    private RoomInfoMapper roomInfoMapper;
    @Autowired
    private GraphInfoService graphInfoService;
    @Autowired
    private RoomFacilityService roomFacilityService;
    @Autowired
    private RoomLabelService roomLabelService;
    @Autowired
    private RoomAttrValueService roomAttrValueService;
    @Autowired
    private RoomPaymentTypeService roomPaymentTypeService;
    @Autowired
    private RoomLeaseTermService roomLeaseTermService;

    @Override
    public void saveOrUpdateRoomInfo(RoomSubmitVo roomSubmitVo) {
        boolean isupdate=roomSubmitVo.getId()!=null;
        super.saveOrUpdate(roomSubmitVo);
        if(isupdate){
            LambdaQueryWrapper<GraphInfo> graphInfoQueryWrapper = new LambdaQueryWrapper<>();
            graphInfoQueryWrapper.eq(GraphInfo::getItemType, ItemType.ROOM);
            graphInfoQueryWrapper.eq(GraphInfo::getItemId, roomSubmitVo.getId());
            graphInfoService.remove(graphInfoQueryWrapper);

            LambdaQueryWrapper<RoomFacility> roomFaciltyQueryWrapper = new LambdaQueryWrapper<>();
            roomFaciltyQueryWrapper.eq(RoomFacility::getRoomId, roomSubmitVo.getId());
            roomFacilityService.remove(roomFaciltyQueryWrapper);

            LambdaQueryWrapper<RoomLabel> roomLabelQueryWrapper = new LambdaQueryWrapper<>();
            roomLabelQueryWrapper.eq(RoomLabel::getRoomId, roomSubmitVo.getId());
            roomLabelService.remove(roomLabelQueryWrapper);

            LambdaQueryWrapper<RoomAttrValue> roomAttrValueQueryWrapper = new LambdaQueryWrapper<>();
            roomAttrValueQueryWrapper.eq(RoomAttrValue::getRoomId, roomSubmitVo.getId());
            roomAttrValueService.remove(roomAttrValueQueryWrapper);

            LambdaQueryWrapper<RoomPaymentType> roomPaymentTypeQueryWrapper = new LambdaQueryWrapper<>();
            roomPaymentTypeQueryWrapper.eq(RoomPaymentType::getRoomId, roomSubmitVo.getId());
            roomPaymentTypeService.remove(roomPaymentTypeQueryWrapper);

            LambdaQueryWrapper<RoomLeaseTerm> roomLeaseTermQueryWrapper = new LambdaQueryWrapper<>();
            roomLeaseTermQueryWrapper.eq(RoomLeaseTerm::getRoomId, roomSubmitVo.getId());
            roomLeaseTermService.remove(roomLeaseTermQueryWrapper);
        }
        List<GraphVo> graphVoList = roomSubmitVo.getGraphVoList();
        if (!CollectionUtils.isEmpty(graphVoList)){
            ArrayList<GraphInfo> graphInfoList = new ArrayList<>();
            for (GraphVo graphVo : graphVoList){
                GraphInfo graphInfo = new GraphInfo();
                graphInfo.setItemType(ItemType.ROOM);
                graphInfo.setItemId(roomSubmitVo.getId());
                graphInfo.setUrl(graphVo.getUrl());
                graphInfo.setName(graphVo.getName());
                graphInfoList.add(graphInfo);
            }
            graphInfoService.saveBatch(graphInfoList);
        }
        List<Long> facilityInfoIDList = roomSubmitVo.getFacilityInfoIds();
        if (!CollectionUtils.isEmpty(facilityInfoIDList)){
            ArrayList<RoomFacility> roomFacilities = new ArrayList<>();
            for (Long facilityInfoId : facilityInfoIDList){
                RoomFacility roomFacility = RoomFacility.builder().roomId(roomSubmitVo.getId()).facilityId(facilityInfoId).build();
                roomFacilities.add(roomFacility);
            }
            roomFacilityService.saveBatch(roomFacilities);
        }
        List<Long> labelInfoIdList = roomSubmitVo.getLabelInfoIds();
        if (!CollectionUtils.isEmpty(labelInfoIdList)){
            ArrayList<RoomLabel> roomLabels = new ArrayList<>();
            for (Long labelInfoId : labelInfoIdList){
                RoomLabel label = RoomLabel.builder().roomId(roomSubmitVo.getId()).labelId(labelInfoId).build();
                roomLabels.add(label);
            }
            roomLabelService.saveBatch(roomLabels);
        }
        List<Long> attrValueIdList = roomSubmitVo.getAttrValueIds();
        if (!CollectionUtils.isEmpty(attrValueIdList)){
            ArrayList<RoomAttrValue> attrValues = new ArrayList<>();
            for (Long attrValueId : attrValueIdList){
                RoomAttrValue attrValue = RoomAttrValue.builder().roomId(roomSubmitVo.getId()).attrValueId(attrValueId).build();
                attrValues.add(attrValue);
            }
            roomAttrValueService.saveBatch(attrValues);
        }
        List<Long> paymentTypeIdList = roomSubmitVo.getPaymentTypeIds();
        if (!CollectionUtils.isEmpty(paymentTypeIdList)){
            ArrayList<RoomPaymentType> paymentTypes = new ArrayList<>();
            for (Long paymentTypeId : paymentTypeIdList){
                RoomPaymentType paymentType = RoomPaymentType.builder().roomId(roomSubmitVo.getId()).paymentTypeId(paymentTypeId).build();
                paymentTypes.add(paymentType);
            }
            roomPaymentTypeService.saveBatch(paymentTypes);
        }
        List<Long> leaseTermIdList = roomSubmitVo.getLeaseTermIds();
        if (!CollectionUtils.isEmpty(leaseTermIdList)){
            ArrayList<RoomLeaseTerm> leaseTerms = new ArrayList<>();
            for (Long leaseTermId : leaseTermIdList){
                RoomLeaseTerm leaseTerm = RoomLeaseTerm.builder().roomId(roomSubmitVo.getId()).leaseTermId(leaseTermId).build();
                leaseTerms.add(leaseTerm);
            }
            roomLeaseTermService.saveBatch(leaseTerms);
        }


    }

    @Override
    public IPage<RoomItemVo> pageItem(Page<RoomItemVo> roomItemVoPage, RoomQueryVo queryVo) {
        return roomInfoMapper.pageItem(roomItemVoPage,queryVo);
    }
}




