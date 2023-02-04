#pragma once

#include <bits/pthread_types.h>
#include <cstdint>
#include <pthread.h>
#include <sys/cdefs.h>
#include <thread>

struct MODS {
    uintptr_t lib_base;
    uintptr_t lib_end;
    bool hooksComplete;
    struct esp {
        bool esp;
        bool esp_line;
        bool esp_box;
    }ESP;
}AKN;

 void *hack_thread(void*) {
     return nullptr;
 }

 __attribute__((constructor))
 void thread_spawns() {
     pthread_t ptId;
     pthread_create(&ptId, nullptr, hack_thread, nullptr);
 }