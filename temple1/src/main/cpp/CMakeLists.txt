cmake_minimum_required(VERSION 3.4.1)

add_library(yuv SHARED IMPORTED )

set_target_properties( yuv
                       PROPERTIES IMPORTED_LOCATION
                       ${CMAKE_SOURCE_DIR}/libyuv/libs/${ANDROID_ABI}/libyuv.so)

add_library( yuvconv
             SHARED
             yuvconv.c)


target_include_directories( yuvconv
                            PRIVATE
                            ${CMAKE_SOURCE_DIR}/libyuv/include )

# Include libraries needed for hello-jni lib
target_link_libraries(yuvconv
                      yuv
                      log)