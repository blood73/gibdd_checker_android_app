#include <jni.h>
#include <string>

extern "C"
JNIEXPORT jstring

JNICALL
Java_ru_bloodsoft_gibddchecker_util_NewWebService_getMsgFromJni(
        JNIEnv *env,
        jobject) {
    std::string hello = "ff";
    return env->NewStringUTF(hello.c_str());
}

extern "C"
JNIEXPORT jstring

JNICALL
Java_ru_bloodsoft_gibddchecker_util_WebService_getHostName(
        JNIEnv *env,
jobject) {
std::string hello = "gg";
return env->NewStringUTF(hello.c_str());
}

extern "C"
JNIEXPORT jstring

JNICALL
Java_ru_bloodsoft_gibddchecker_ui_PlateActivity_getEaistoUrl(
        JNIEnv *env,
        jobject) {
    std::string hello = "Gdfgu";
    return env->NewStringUTF(hello.c_str());
}

extern "C"
JNIEXPORT jstring

JNICALL
Java_ru_bloodsoft_gibddchecker_ui_PlateActivity_getVinUrl(
        JNIEnv *env,
        jobject) {
    std::string hello = "dfg";
    return env->NewStringUTF(hello.c_str());
}

extern "C"
JNIEXPORT jstring

JNICALL
Java_ru_bloodsoft_gibddchecker_ui_MileageActivity_getMileageUrl(
        JNIEnv *env,
        jobject) {
    std::string hello = "df";
    return env->NewStringUTF(hello.c_str());
}

extern "C"
JNIEXPORT jstring

JNICALL
Java_ru_bloodsoft_gibddchecker_ui_MileageActivity_getCheckUserUrl(
        JNIEnv *env,
        jobject) {
    std::string hello = "df";
    return env->NewStringUTF(hello.c_str());
}

extern "C"
JNIEXPORT jstring

JNICALL
Java_ru_bloodsoft_gibddchecker_ui_EaistoActivity_getEaistoUrl(
        JNIEnv *env,
        jobject) {
    std::string hello = "dff";
    return env->NewStringUTF(hello.c_str());
}

extern "C"
JNIEXPORT jstring

JNICALL
Java_ru_bloodsoft_gibddchecker_ui_MileageInappActivity_getCheckUserUrl(
        JNIEnv *env,
        jobject) {
    std::string hello = "ddx";
    return env->NewStringUTF(hello.c_str());
}

extern "C"
JNIEXPORT jstring

JNICALL
Java_ru_bloodsoft_gibddchecker_ui_MyReportsActivity_getUrlGetReports(
        JNIEnv *env,
        jobject) {
    std::string hello = "fvsdfq";
    return env->NewStringUTF(hello.c_str());
}

extern "C"
JNIEXPORT jstring

JNICALL
Java_ru_bloodsoft_gibddchecker_ui_FullReportActivity_getUrlGetVehicle(
        JNIEnv *env,
        jobject) {
    std::string hello = "sgh";
    return env->NewStringUTF(hello.c_str());
}

extern "C"
JNIEXPORT jstring

JNICALL
Java_ru_bloodsoft_gibddchecker_ui_FullReportActivity_getUrlGibddRequest(
        JNIEnv *env,
        jobject) {
    std::string hello = "ddss";
    return env->NewStringUTF(hello.c_str());
}

extern "C"
JNIEXPORT jstring

JNICALL
Java_ru_bloodsoft_gibddchecker_ui_FullReportActivity_getUrlCreateReport(
        JNIEnv *env,
        jobject) {
    std::string hello = "fdfjk";
    return env->NewStringUTF(hello.c_str());
}

extern "C"
JNIEXPORT jstring

JNICALL
Java_ru_bloodsoft_gibddchecker_ui_recycler_1views_ReportItemRecyclerViewAdapter_getApiUrl(
        JNIEnv *env,
        jobject) {
    std::string hello = "dhfg";
    return env->NewStringUTF(hello.c_str());
}

extern "C"
JNIEXPORT jstring

JNICALL
Java_ru_bloodsoft_gibddchecker_ui_FullReportActivity_getUrlGetGibddData(
        JNIEnv *env,
        jobject) {
    std::string hello = "dfgkl";
    return env->NewStringUTF(hello.c_str());
}

extern "C"
JNIEXPORT jstring

JNICALL
Java_ru_bloodsoft_gibddchecker_ui_quote_ArticleDetailFragment_getUrlGetGibddData(
        JNIEnv *env,
        jobject) {
    std::string hello = "dksf";
    return env->NewStringUTF(hello.c_str());
}