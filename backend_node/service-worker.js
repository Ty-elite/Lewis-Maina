/**
 * ==========================================
 * KENYARENT PWA SERVICE WORKER (sw.js)
 * ==========================================
 * Enables complete offline viewing and resource caches.
 */

const CACHE_NAME = 'kenyarent-cache-v1';

// Assets to preload initially for immediate loads
const PRECACHE_ASSETS = [
    '/',
    '/index.html',
    '/styles.css',
    '/app.js',
    '/offline.html',
    '/icons/icon-192.png'
];

// 1. Install Event: Cache Static Assets
self.addEventListener('install', (event) => {
    event.waitUntil(
        caches.open(CACHE_NAME)
            .then((cache) => {
                console.log('Pre-caching offline asset shells...');
                return cache.addAll(PRECACHE_ASSETS);
            })
            .then(() => self.skipWaiting())
    );
});

// 2. Activate Event: Cleanup stale caches
self.addEventListener('activate', (event) => {
    event.waitUntil(
        caches.keys().then((keys) => {
            return Promise.all(
                keys.map((key) => {
                    if (key !== CACHE_NAME) {
                        console.log('Purging old offline cache logs:', key);
                        return caches.delete(key);
                    }
                })
            );
        }).then(() => self.clients.claim())
    );
});

// 3. Fetch Event interceptor: Network-first falling back to offline cache
self.addEventListener('fetch', (event) => {
    const req = event.request;

    // Only intercept local or GET requests to avoid interrupting mutation writes
    if (req.method !== 'GET') {
        return;
    }

    // Handle listings searches cache dynamically
    if (req.url.includes('/api/properties') || req.url.includes('unsplash.com')) {
        event.respondWith(
            fetch(req)
                .then((networkResponse) => {
                    // Clone response to pipe into cache
                    const responseClone = networkResponse.clone();
                    caches.open(CACHE_NAME).then((cache) => {
                        cache.put(req, responseClone);
                    });
                    return networkResponse;
                })
                .catch(() => {
                    console.log('App is offline. Pulling properties from Service Worker Cache...');
                    return caches.match(req);
                })
        );
        return;
    }

    // Default Cache or Network strategy:
    event.respondWith(
        caches.match(req).then((cachedResponse) => {
            if (cachedResponse) {
                return cachedResponse;
            }

            return fetch(req).catch(() => {
                // Return offline fallback placeholder if asset missing offline
                if (req.mode === 'navigate') {
                    return caches.match('/offline.html');
                }
            });
        })
    );
});
