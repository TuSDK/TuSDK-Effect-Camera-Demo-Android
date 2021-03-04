# 涂图·流处理（特效相机） SDK 产品介绍

## 概述

涂图流处理（特效相机） SDK 是涂图基于特效渲染引擎与采集相结合的 SDK 产品。产品分为 2 个分类，一种是使用完整的涂图相机 + 特效渲染引擎；一种是使用第三方采集 + 涂图特效渲染引擎。
前者更多是适用于自采集和视频录制场景，后者更多是适用于使用第三方直播 SDK 场景使用。

目前该 SDK 提供多项特效包括但不限于：

- 智能美肤；
- AI 微整形特效；
- AI 动态贴纸特效；
- AI 哈哈镜特效；
- AI 美妆
- 滤镜特效；
- 漫画滤镜；
- 变声特效；

## 功能和特效介绍：

当前在涂图流处理（特效相机） SDK 中，除了提供特效外，相机录制和拍照提供了如下内容：

<table cellpadding="0" cellspacing="0"  style="width:720px;border-collapse: collapse;">
    <thead>
      <tr>
        <th width="110">模块</th>
        <th  width="110">功能点</th>
        <th  width="400">功能介绍</th>
        <th  width="100">版本</th>
      </tr>
    </thead>
    <tbody>
        <tr >
            <td rowspan="6" >生成输出</td>
            <td >UI界面自定义</td>
            <td >开发者可完全自定义 UI 界面。</td>
            <td >1.0.0（+）</td>
        </tr>
        <tr>
            <td>设定输出的尺寸</td>
            <td >可以设定需要输出的分辨率和码率。</td>
            <td >1.0.0（+）</td>
        </tr>
        <tr>
            <td>硬编码器支持</td>
            <td >默认硬件编码器，支持切换软件编码器进行生成。</td>
            <td >1.0.0（+）</td>
        </tr>
        <tr>
            <td>设定视频水印</td>
            <td >可以添加一个水印贴纸（后台生成）作为视频水印；水印位置支持自定义设置。</td>
            <td >1.0.0（+）</td>
        </tr>
        <tr>
            <td>生成视频</td>
            <td >最终打包生成视频，生成 MP4 或 MOV 视频格式。</td>
            <td >1.0.0（+）</td>
        </tr>
        <tr>
            <td>视频压缩</td>
            <td >支持输出文件格式 MP4，支持输出文件码率设置，支持设置压缩比。</td>
            <td >1.0.0（+）</td>
        </tr>
        <tr >
            <td rowspan="20" >相机录制（拍摄）</td>
            <td >全高清录制</td>
            <td >支持最高 1080P 视频录制。</td>
            <td >1.0.0（+）</td>
        </tr>
        <tr>
            <td>多段录制</td>
            <td >实现连续多次拍摄，断点续拍。</td>
            <td >1.0.0（+）</td>
        </tr>
        <tr>
            <td>不限时长</td>
            <td >无录制时长限制，支持自定义设定录制时长。</td>
            <td >1.0.0（+）</td>
        </tr>
        <tr>
            <td>摄像头切换</td>
            <td >录制时支持摄像头的前后切换。</td>
            <td >1.0.0（+）</td>
        </tr>
        <tr>
            <td>闪光灯</td>
            <td >录制时支持闪光灯开启与关闭，用于弱光时的补偿。</td>
            <td >1.0.0（+）</td>
        </tr>
        <tr>
            <td>画面对焦</td>
            <td >录制时支持对画面进行对焦点设定，支持自动和手动。</td>
            <td >1.0.0（+）</td>
        </tr>
        <tr>
            <td>视频变速</td>
            <td >录制时支持拍摄速度的调整，快慢速。</td>
            <td >1.0.0（+）</td>
        </tr>
        <tr>
            <td>拍摄比例</td>
            <td >录制时可自由设定画面比例，如 1:1、16:9、4:3。</td>
            <td >1.0.0（+）</td>
        </tr>
        <tr>
            <td>多种拍摄方式</td>
            <td >支持 3 种拍摄方式：拍照，长按录制，点按录制。</td>
            <td >1.0.0（+）</td>
        </tr>
        <tr>
            <td>自定义分辨率</td>
            <td >自定义输出分辨率（影响画面尺寸）。</td>
            <td >1.0.0（+）</td>
        </tr>
        <tr>
            <td>自定义码率</td>
            <td >自定义输出码率（影响视频画面清晰度，及文件大小）。</td>
            <td >1.0.0（+）</td>
        </tr>
        <tr>
            <td>水印</td>
            <td >支持自定义水印，在视频画面中增加水印信息。</td>
            <td >1.0.0（+）</td>
        </tr>
        <tr>
            <td>美肤</td>
            <td >3 套不同效果的美肤算法。</td>
            <td >1.0.0（+）</td>
        </tr>
        <tr>
            <td>变声特效</td>
            <td >支持视频录制时的原音变声处理，萝莉、女生、大叔、怪兽 4 种特效。</td>
            <td >1.0.0（+）</td>
        </tr>
        <tr>
            <td>滤镜</td>
            <td >多款调色、主题滤镜，实时预览。支持不同效果参数调节。</td>
            <td >1.0.0（+）</td>
        </tr>
        <tr>
            <td>漫画滤镜</td>
            <td >类 “iPhone” 的动漫滤镜效果，提供国漫、美漫、日漫、淡彩等效果。</td>
            <td >1.0.0（+）</td>
        </tr>
        <tr>
            <td>动态贴纸（基于人脸识别）</td>
            <td >基于人脸识别技术，提供多款 2D 动态贴纸，支持在线下载和本地删除。</td>
            <td >1.0.0（+）</td>
        </tr>
        <tr>
            <td>微整形（基于人脸识别）</td>
            <td >基于人脸识别技术，支持对大眼、瘦脸、瘦鼻、嘴型、眉型、下巴、眼角、眼距等 26 个效果的调节；支持多张人脸同时调整。</td>
            <td >1.0.0（+）</td>
        </tr>
        <tr>
            <td>哈哈镜特效（基于人脸识别）</td>
            <td >基于人脸识别技术，提供多种不同效果的哈哈镜（即人脸变形）特效。</td>
            <td >1.0.0（+）</td>
        </tr>
        <tr>
            <td>美妆（基于人脸识别）</td>
            <td >基于人脸识别技术，106 关键点实现。提供面部的美妆，包含但不限于口红、眼影、睫毛、眼线、腮红、眉毛等。</td>
            <td >1.0.0（+）</td>
        </tr>
        <tr >
            <td rowspan="2" >滤镜特效</td>
            <td >基础滤镜</td>
            <td >丰富的滤镜效果，包括调色等多种滤镜。</td>
            <td >1.0.0（+）</td>
        </tr>
        <tr>
            <td>漫画滤镜</td>
            <td >类 “iPhone” 的动漫滤镜效果，提供国漫、美漫、日漫、淡彩等效果。</td>
            <td >1.0.0（+）</td>
        </tr>
        <tr >
            <td rowspan="5" >动态贴纸特效</td>
            <td >动态贴纸</td>
            <td >基于人脸识别的 2D 动态贴纸，200+ 组可选。</td>
            <td >1.0.0（+）</td>
        </tr>
        <tr>
            <td>多次添加贴纸</td>
            <td >可以在视频制作中，支持不同视频段拍摄增加不同的动态贴纸。</td>
            <td >1.0.0（+）</td>
        </tr>
        <tr>
            <td>多人脸添加贴纸</td>
            <td >支持在视频录制时在多张人脸中添加贴纸。</td>
            <td >1.0.0（+）</td>
        </tr>
        <tr>
            <td>贴纸自定义</td>
            <td >支持用户自定义上传，提供设计规范，允许用户上传自行设计的贴纸。</td>
            <td >1.0.0（+）</td>
        </tr>
        <tr>
            <td>动态下载</td>
            <td >支持贴纸在线下载。</td>
            <td >1.0.0（+）</td>
        </tr>
        <tr >
            <td rowspan="3" >美肤特效</td>
            <td >自然美肤</td>
            <td >全新自然美肤，让美肤更自然。提供磨皮、美白、锐化调节。</td>
            <td >1.0.0（+）</td>
        </tr>
        <tr>
            <td>极致美肤</td>
            <td >突破美肤极致，让面部光滑无极限。提供磨皮、美白、红润。</td>
            <td >1.0.0（+）</td>
        </tr>
        <tr>
            <td>精准美肤</td>
            <td >保留面部纹理，让美肤更真实。提供磨皮、美白、红润调节。</td>
            <td >1.0.0（+）</td>
        </tr>
        <tr >
            <td rowspan="26" >微整形特效</td>
            <td >大眼</td>
            <td >眼睛大小的调整。</td>
            <td >1.0.0（+）</td>
        </tr>
        <tr>
            <td>瘦脸</td>
            <td >颧骨以下面部的向内收缩。</td>
            <td >1.0.0（+）</td>
        </tr>
        <tr>
            <td>窄脸</td>
            <td >颧骨以下的脸颊宽度调节。</td>
            <td >1.0.0（+）</td>
        </tr>
        <tr>
            <td>小脸</td>
            <td >脸颊下部的大小调节。</td>
            <td >1.0.0（+）</td>
        </tr>
        <tr>
            <td>瘦鼻</td>
            <td >鼻翼宽度调节。</td>
            <td >1.0.0（+）</td>
        </tr>
        <tr>
            <td>长鼻</td>
            <td >鼻子长短调节。</td>
            <td >1.0.0（+）</td>
        </tr>
        <tr>
            <td>嘴型</td>
            <td >嘴巴大小调节。</td>
            <td >1.0.0（+）</td>
        </tr>
        <tr>
            <td>唇厚</td>
            <td >嘴唇厚度调节。</td>
            <td >1.0.0（+）</td>
        </tr>
        <tr>
            <td>缩人中</td>
            <td >人中长短调节。</td>
            <td >1.0.0（+）</td>
        </tr>
        <tr>
            <td>细眉</td>
            <td >眉毛粗细调节。</td>
            <td >1.0.0（+）</td>
        </tr>
        <tr>
            <td>眉高</td>
            <td >眉毛高低调节。</td>
            <td >1.0.0（+）</td>
        </tr>
        <tr>
            <td>下巴</td>
            <td >下巴长短调节。</td>
            <td >1.0.0（+）</td>
        </tr>
        <tr>
            <td>下颌骨</td>
            <td >下颌骨宽度调节。</td>
            <td >1.0.0（+）</td>
        </tr>
        <tr>
            <td>眼角</td>
            <td >眼睛角度调节。</td>
            <td >1.0.0（+）</td>
        </tr>
        <tr>
            <td>开内眼角</td>
            <td >内眼角角度调节。</td>
            <td >1.0.0（+）</td>
        </tr>
        <tr>
            <td>开外眼角</td>
            <td >外眼角角度调节。</td>
            <td >1.0.0（+）</td>
        </tr>
        <tr>
            <td>眼距</td>
            <td >眼睛左右间距调节。</td>
            <td >1.0.0（+）</td>
        </tr>
        <tr>
            <td>眼移动</td>
            <td >眼睛上下位置调节。</td>
            <td >1.0.0（+）</td>
        </tr>
        <tr>
            <td>发际线</td>
            <td >发际线高低调节。</td>
            <td >1.0.0（+）</td>
        </tr>
        <tr>
            <td>瘦颧骨</td>
            <td >颧骨宽度调节。</td>
            <td >1.0.0（+）</td>
        </tr>
        <tr>
            <td>双眼皮</td>
            <td >添加双眼皮。</td>
            <td >1.0.0（+）</td>
        </tr>
        <tr>
            <td>卧蚕</td>
            <td >眼底部添加卧蚕。</td>
            <td >1.0.0（+）</td>
        </tr>
        <tr>
            <td>白牙</td>
            <td >进行牙齿美白，使牙齿变白。</td>
            <td >1.0.0（+）</td>
        </tr>
        <tr>
            <td>亮眼</td>
            <td >增加眼睛部分亮度，使眼睛更加有神。</td>
            <td >1.0.0（+）</td>
        </tr>
        <tr>
            <td>黑眼圈</td>
            <td >去除眼睛的黑眼圈。</td>
            <td >1.0.0（+）</td>
        </tr>
        <tr>
            <td>法令纹</td>
            <td >去除眼部的法令纹。</td>
            <td >1.0.0（+）</td>
        </tr>
        <tr >
            <td rowspan="8" >美妆特效</td>
            <td >口红</td>
            <td >提供雾面、滋润、水润 3 种质地，只需赋予 RGB 颜色，即可完成效果设定。</td>
            <td >1.0.0（+）</td>
        </tr>
        <tr>
            <td>眉毛</td>
            <td >提供雾眉、雾根眉 2 种画法，13 个眉形。满足大多数人脸眉形需要。</td>
            <td >1.0.0（+）</td>
        </tr>
        <tr>
            <td>眼影</td>
            <td >提提供 23 种眼影妆容，多种画法和色彩组合。</td>
            <td >1.0.0（+）</td>
        </tr>
        <tr>
            <td>眼线</td>
            <td >提供 16 种眼线妆容，多种画法和色彩组合。</td>
            <td >1.0.0（+）</td>
        </tr>
        <tr>
            <td>睫毛</td>
            <td >提供 29 种睫毛妆容，多种画法和色彩组合。</td>
            <td >1.0.0（+）</td>
        </tr>
        <tr>
            <td>腮红</td>
            <td >提供 16 种腮红妆容，多种画法和色彩组合。</td>
            <td >1.0.0（+）</td>
        </tr>
        <tr>
            <td>粉底</td>
            <td >修饰面部肤色调节气色。</td>
            <td >1.0.0（+）</td>
        </tr>
        <tr>
            <td>修容</td>
            <td >调整面部高光和阴影。</td>
            <td >1.0.0（+）</td>
        </tr>
        <tr >
            <td rowspan="1" >哈哈镜特效</td>
            <td >哈哈镜特效</td>
            <td >提供多款人脸变形（哈哈镜）特效：大鼻子、大饼脸、国字脸、厚嘴唇、眯眯眼、木瓜脸、蛇精脸等。</td>
            <td >1.0.0（+）</td>
        </tr>
    </tbody>
</table>

## 对接第三方情况

目前流处理（特效相机） SDK 已与对家直播（实时音视频）厂商合作，已服务多家不同行业和类型的直播平台。

目前已对接完成的第三方直播（实时音视频）厂商及其产品情况如下：


### 七牛云

<table cellpadding="0" cellspacing="0"  style="width:700px;border-collapse: collapse;">
    <thead>
      <tr>
        <th  width="150">第三方产品</th>
	<th  width="150">平台</th>
        <th  width="200">第三方 SDK 版本</th>
	<th  width="200">涂图 SDK 版本</th>
      </tr>
    </thead>
    <tbody>
        <tr >
            <td rowspan="2">直播</td>
            <td>iOS</td>
	    	<td>2.3.4(+)</td>
	    	<td>3.5.4(+)</td>
        </tr>
        <tr >
            <td>Android</td>
			<td>2.4.0(+)</td>
	    	<td>3.5.5(+)</td>
        </tr>
		<tr >
			<td rowspan="2">连麦</td>
            <td>iOS</td>
			<td>2.3.0(+)</td>
	    	<td>3.5.4(+)</td>
        </tr>
		<tr >
            <td>Android</td>
			<td>2.3.0(+)</td>
	    	<td>3.5.5(+)</td>
        </tr>
		<tr >
			<td rowspan="2">短视频</td>
            <td>iOS</td>
			<td>3.1.1(+)</td>
	    	<td>3.5.4(+)</td>
        </tr>
		<tr >
            <td>Android</td>
			<td>2.1.0(+)</td>
	    	<td>3.5.5(+)</td>
        </tr>
	</tbody>
</table>

### 网易

<table cellpadding="0" cellspacing="0"  style="width:700px;border-collapse: collapse;">
    <thead>
      <tr>
        <th  width="150">第三方产品</th>
	<th  width="150">平台</th>
        <th  width="200">第三方 SDK 版本</th>
	<th  width="200">涂图 SDK 版本</th>
      </tr>
    </thead>
    <tbody>
        <tr >
            <td rowspan="2">实时音视频</td>
            <td>iOS</td>
	    	<td>7.0.0(+)</td>
	    	<td>3.5.4(+)</td>
        </tr>
        <tr >
            <td>Android</td>
            <td>-</td>
	    <td>-</td>
        </tr>
    </tbody>
</table>

### 即构

<table cellpadding="0" cellspacing="0"  style="width:700px;border-collapse: collapse;">
    <thead>
      <tr>
        <th  width="150">第三方产品</th>
	<th  width="150">平台</th>
        <th  width="200">第三方 SDK 版本</th>
	<th  width="200">涂图 SDK 版本</th>
      </tr>
    </thead>
    <tbody>
        <tr >
            <td rowspan="2">连麦</td>
            <td>iOS</td>
	    <td>Update iOS SDK to 2020.3.9 release</td>
	    <td>3.5.4(+)</td>
        </tr>
        <tr >
            <td>Android</td>	    	
	    <td>200312_054102_release_new-0-gc9a8555_bn1169</td>
	    <td>3.5.5(+)</td>
        </tr>
	</tbody>
</table>


### 声网

<table cellpadding="0" cellspacing="0"  style="width:700px;border-collapse: collapse;">
    <thead>
      <tr>
        <th  width="150">第三方产品</th>
	<th  width="150">平台</th>
        <th  width="200">第三方 SDK 版本</th>
	<th  width="200">涂图 SDK 版本</th>
      </tr>
    </thead>
    <tbody>
        <tr >
            <td rowspan="2">直播</td>
            <td>iOS</td>
	    	<td>3.0.0(+)</td>
	    	<td>3.5.4(+)</td>
        </tr>
        <tr >
            <td>Android</td>    	
			<td>2.8.0(+)</td>
	    	<td>3.5.5(+)</td>
        </tr>
	</tbody>
</table>


### 三体云

<table cellpadding="0" cellspacing="0"  style="width:700px;border-collapse: collapse;">
    <thead>
      <tr>
        <th  width="150">第三方产品</th>
	<th  width="150">平台</th>
        <th  width="200">第三方 SDK 版本</th>
	<th  width="200">涂图 SDK 版本</th>
      </tr>
    </thead>
    <tbody>
        <tr >
            <td rowspan="2">连麦</td>
            <td>iOS</td>
	    	<td>2.7.0(+)</td>
	    	<td>3.5.4(+)</td>
        </tr>
        <tr >
            <td>Android</td>	    	
			<td>2.7.1(+)</td>
	    	<td>3.5.5(+)</td>
        </tr>
	</tbody>
</table>


### 阿里云

<table cellpadding="0" cellspacing="0"  style="width:700px;border-collapse: collapse;">
    <thead>
      <tr>
        <th  width="150">第三方产品</th>
	<th  width="150">平台</th>
        <th  width="200">第三方 SDK 版本</th>
	<th  width="200">涂图 SDK 版本</th>
      </tr>
    </thead>
    <tbody>
        <tr >
            <td rowspan="2">短视频</td>
            <td>iOS</td>
	    	<td>1.3.0(+)</td>
	    	<td>3.5.4(+)</td>
        </tr>
        <tr >
            <td>Android</td>    	
			<td>1.5.1(+)</td>
	    	<td>3.5.5(+)</td>
        </tr>
	</tbody>
</table>


### 腾讯云

<table cellpadding="0" cellspacing="0"  style="width:700px;border-collapse: collapse;">
    <thead>
      <tr>
        <th  width="150">第三方产品</th>
	<th  width="150">平台</th>
        <th  width="200">第三方 SDK 版本</th>
	<th  width="200">涂图 SDK 版本</th>
      </tr>
    </thead>
    <tbody>
        <tr >
            <td rowspan="2">直播</td>
            <td>iOS</td>
	    	<td>5.3.0(+)</td>
	    	<td>3.5.4(+)</td>
        </tr>
        <tr >
            <td>Android</td>    	
			<td>6.8(+)</td>
	    	<td>3.5.5(+)</td>
        </tr>
	</tbody>
</table>


因对接时间的影响上述第三方 SDK 的版本号仅供参考，如需对接测试，请联系商务并告知您使用的方案和具体需要的对接版本。

## 设备及系统要求

设备要求：搭载 Android 和 iOS 系统的手机设备

系统要求：Android 5.0 和 iOS 9.0 及其以上

## 商务合作

电话：177-6716-7529

邮箱：sales@tusdk.com

QQ：2969573855

联系地址：浙江省杭州市西湖区 西斗门路 9号 福地创业园1号楼3楼



