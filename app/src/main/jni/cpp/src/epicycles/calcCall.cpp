#include <jni.h>
#include "../../third_party/my-cpp-lib/FourierSeries.h"
#include "../jni_h/pers_zhc_tools_jni_JNI_FourierSeries.h"
#include "../jni_help.h"

using namespace bczhc;

class Point {
public:
    float x, y;

    Point() = default;

    Point(float x, float y) : x(x), y(y) {}
};

class F : public ComplexFunctionInterface {
private:
    SequentialList<Point> &list;
    double period, scale{};
    double pathsTotalLength{};
    double *sumLength = nullptr;
    int listLength;

    static double getPathLength(Point &p1, Point &p2) {
        return sqrt(pow(p1.x - p2.x, 2) + pow(p1.y - p2.y, 2));
    }

    static int search(const double *arr, int length, double target) {
        for (int i = 0; i < length - 1; ++i) {
            if (target > arr[i] && target < arr[i + 1]) return i;
        }
        if (target > arr[length - 1]) return length - 1;
        return -1;
    }

    static Point linearMoveBetweenTwoPoints(Point p1, Point p2, double progress) {
        Point p((float) (p1.x + (p2.x - p1.x * progress)), (float) (p1.y + (p2.y - p1.y) * progress));
        return p;
    }

public:

    explicit F(SequentialList<Point> &list, double period) : list(list), period(period) {
        listLength = list.length();
        sumLength = new double[listLength];
        for (int i = 0; i < listLength; ++i) {
            Point next = i == listLength - 1 ? list.get(0) : list.get(i + 1);
            Point currPoint = list.get(i);
            pathsTotalLength += getPathLength(currPoint, next);
            sumLength[i] = pathsTotalLength;
        }
    }

    ~F() {
        delete[] sumLength;
    }

    void x(ComplexValue &dest, double t) override {
        t = t - (t >= period ? floor(t / period) * period : 0);
        double mapToLength = t * pathsTotalLength / period;
        int index = search(sumLength, listLength, mapToLength);
        Point r = linearMoveBetweenTwoPoints(list.get(index),
                                             index == listLength - 1 ? list.get(0) : list.get(index + 1),
                                             mapToLength - sumLength[index]);
        dest.re = r.x, dest.im = r.y;
    }
};

using namespace linearlist;

JNIEXPORT void JNICALL Java_pers_zhc_tools_jni_JNI_00024FourierSeries_calc
        (JNIEnv *env, jclass cls, jobject points, jdouble period, jint epicyclesCount, jobject callback,
         jint threadNum, jint integralD) {
    jclass listCLass = env->GetObjectClass(points);
    jmethodID sizeMId = env->GetMethodID(listCLass, "size", "()I");
    int length = (int) env->CallIntMethod(points, sizeMId);
    jmethodID getMId = env->GetMethodID(listCLass, "get", "(I)Ljava/lang/Object;");
    SequentialList<Point> list;
    for (int i = 0; i < length; ++i) {
        jobject complexValueObj = env->CallObjectMethod(points, getMId, (jint) i);
        jclass complexValueClass = env->GetObjectClass(complexValueObj);
        jfieldID reFId = env->GetFieldID(complexValueClass, "re", "D");
        jfieldID imFId = env->GetFieldID(complexValueClass, "im", "D");
        Point p(env->GetDoubleField(complexValueObj, reFId), env->GetDoubleField(complexValueObj, imFId));
        list.insert(p);

        string s = to_string(p.x);
        s.append(" ");
        s.append(to_string(p.y));
        Log(env, "jni---insert", s.c_str());
    }

    F f(list, period);

    jclass callbackClass = env->GetObjectClass(callback);
    jmethodID callbackMId = env->GetMethodID(callbackClass, "callback", "(DDD)V");

    Log(env, "jni---", "1");

    class Callback : public FourierSeriesCallback {
    public:
        jobject &callbackObject;
        jmethodID &callbackMId;
        JNIEnv *&env;

        void callback(double n, double re, double im) override {
//            env->CallVoidMethod(callbackObject, callbackMId, (jdouble) n, (jdouble) re, (jdouble) im);
            string s = to_string(n);
            s.append(" ");
            s.append(to_string(re));
            s.append(" ");
            s.append(to_string(im));
            Log(env, "jni---", s.c_str());
        }

        Callback(jobject &callbackObject, jmethodID &callbackMId, JNIEnv *&env) : callbackObject(callbackObject),
                                                                                  callbackMId(callbackMId), env(env) {}
    } cb(callback, callbackMId, env);

    Log(env, "jni---", "3");

    class CB2 : public FourierSeriesCallback {
    private:
        JNIEnv *&env;
    public:
        CB2(JNIEnv *&env) : env(env) {}

        void callback(double n, double re, double im) override {
            string s = to_string(n);
            s.append(" ");
            s.append(to_string(re));
            s.append(" ");
            s.append(to_string(im));
            Log(env, "jni---", s.c_str());
        }
    } cb2(env);

    FourierSeries fs(f, 100, 10);
    fs.calc(cb2, 10000, 3);

    Log(env, "jni---", "2");
}