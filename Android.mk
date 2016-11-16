LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_JAVA_LIBRARIES := #bouncycastle conscrypt telephony-common
LOCAL_STATIC_JAVA_LIBRARIES := cell commons-codec commons-net-ftp simple-xml android-support-v4

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/res

LOCAL_PACKAGE_NAME := BurnIn
LOCAL_CERTIFICATE := platform
LOCAL_PROGUARD_FLAG_FILES := proguard.cfg



include $(BUILD_PACKAGE)
include $(CLEAR_VARS)

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := \
    cell:libs/cell.jar \
    commons-codec:libs/commons-codec-1.9.jar \
    commons-net-ftp:libs/commons-net-ftp-2.0.jar \
    simple-xml:libs/simple-xml-2.7.1.jar \
    android-support-v4:libs/android-support-v4.jar

include $(BUILD_MULTI_PREBUILT)
include $(call all-makefiles-under,$(LOCAL_PATH))
