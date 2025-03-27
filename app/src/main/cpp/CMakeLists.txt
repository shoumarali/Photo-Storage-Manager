cmake_minimum_required(VERSION 3.31.6)

add_library(
        root_checker
        SHARED
        root_checker.cpp
)

find_library(
        log_lib
        log
)

target_link_libraries(
        root_checker
        ${log_lib}
)