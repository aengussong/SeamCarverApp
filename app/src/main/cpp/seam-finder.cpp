#include "seam-finder.h"
#include <jni.h>
#include <vector>
#include <limits>
#include <algorithm>
#include <android/log.h>

using namespace std;

extern "C" JNIEXPORT jintArray JNICALL
Java_com_aengussong_seamcarver_algorithm_SeamCarver_findVerticalSeam(JNIEnv* env, jobject obj, jobjectArray energyArray) {
    // copying an array takes 300ms
    int width = env->GetArrayLength(energyArray);
    int height = env->GetArrayLength((jdoubleArray)env->GetObjectArrayElement(energyArray, 0));

    // Convert energyArray (Java double[][]) to a C++ 2D vector
    vector<vector<double>> energy(width, vector<double>(height));
    for (int i = 0; i < width; ++i) {
        jdoubleArray row = (jdoubleArray)env->GetObjectArrayElement(energyArray, i);
        jdouble* rowElements = env->GetDoubleArrayElements(row, 0);
        for (int j = 0; j < height; ++j) {
            energy[i][j] = rowElements[j];
        }
        env->ReleaseDoubleArrayElements(row, rowElements, 0);
    }

    __android_log_print(ANDROID_LOG_INFO, "ctag", "%s", "start searching for seam");
    // C++ code for seam finding
    vector<vector<int>> path(width, vector<int>(height));
    vector<vector<double>> values(width, vector<double>(height, numeric_limits<double>::max()));

    for (int i = 0; i < width; ++i) {
        values[i][0] = energy[i][0];
    }
    for (int y = 0; y < height - 1; ++y) {
        for (int x = 1; x < width - 1; ++x) {
            double currentValue = values[x][y];
            double energyLeft = energy[x - 1][y + 1];
            double energyCenter = energy[x][y + 1];
            double energyRight = energy[x + 1][y + 1];

            if (currentValue + energyLeft < values[x - 1][y + 1]) {
                values[x - 1][y + 1] = currentValue + energyLeft;
                path[x - 1][y + 1] = x;
            }
            if (currentValue + energyCenter < values[x][y + 1]) {
                values[x][y + 1] = currentValue + energyCenter;
                path[x][y + 1] = x;
            }
            if (currentValue + energyRight < values[x + 1][y + 1]) {
                values[x + 1][y + 1] = currentValue + energyRight;
                path[x + 1][y + 1] = x;
            }
        }
    }

    // Find the shortest path in the last row
    double shortest = numeric_limits<double>::max();
    int shortestIndex = 0;
    for (int x = 0; x < width; ++x) {
        if (values[x][height - 1] < shortest) {
            shortest = values[x][height - 1];
            shortestIndex = x;
        }
    }

    // Backtrack to determine the seam path
    vector<int> verticalSeam(height);
    int nextIndex = shortestIndex;
    for (int y = height - 1; y >= 0; --y) {
        verticalSeam[y] = nextIndex;
        nextIndex = path[nextIndex][y];
    }

    // Convert C++ vector to jintArray for returning to Java/Kotlin
    jintArray result = env->NewIntArray(height);
    env->SetIntArrayRegion(result, 0, height, verticalSeam.data());
    return result;
}
