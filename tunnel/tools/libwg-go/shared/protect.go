package shared

/*
#include "vpn_jni.h"
*/
import "C"
import "syscall"

const ipv6TrafficClass = 67 // IPV6_TCLASS on Linux/Android

// ProtectControlFunc control hook to bypass sockets and apply gaming-optimized socket options.
// Sets UDP socket buffers to 2 MB (SO_SNDBUF/SO_RCVBUF) to eliminate packet loss under peak
// load (Discord, Telegram). Sets IP ToS to 0xB8 (Expedited Forwarding) so MediaTek Helio
// and home routers prioritise WireGuard UDP packets ahead of bulk traffic (Roblox, Discord).
func ProtectControlFunc(network, address string, c syscall.RawConn) error {
	var opErr error
	err := c.Control(func(fd uintptr) {
		// --- 1. Bypass the VPN for this socket (existing behaviour) ---
		if C.bypass_socket(C.int(fd)) == 0 {
			opErr = syscall.EACCES
			LogError("Protect", "Failed to protect socket FD: %d", fd)
		} else {
			LogDebug("Protect", "Protected socket FD: %d", fd)
		}

		// --- 2. UDP buffer optimisation: 2 MB send + receive buffers ---
		// Eliminates packet loss for Discord/Telegram under peak UDP load.
		// The Linux kernel doubles the value internally; we request 2 MB to
		// land in the ~2 MB effective range on Android.
		const bufSize = 2 * 1024 * 1024
		if err := syscall.SetsockoptInt(int(fd), syscall.SOL_SOCKET, syscall.SO_SNDBUF, bufSize); err != nil {
			LogDebug("Protect", "SO_SNDBUF set failed (fd %d): %v", fd, err)
		}
		if err := syscall.SetsockoptInt(int(fd), syscall.SOL_SOCKET, syscall.SO_RCVBUF, bufSize); err != nil {
			LogDebug("Protect", "SO_RCVBUF set failed (fd %d): %v", fd, err)
		}

		// --- 3. QoS / Type of Service: Expedited Forwarding (0xB8) ---
		// Forces the CPU and upstream routers to schedule WireGuard UDP
		// packets ahead of bulk traffic — critical for low-latency gaming
		// and real-time voice (Roblox, Discord, Telegram calls).
		// 0xB8 = DSCP EF (101110xx) — highest priority queue on most hardware.
		const tos = 0xB8
		// IPv4 ToS
		_ = syscall.SetsockoptInt(int(fd), syscall.IPPROTO_IP, syscall.IP_TOS, tos)
		// IPv6 Traffic Class (IPV6_TCLASS = 67); ignore error on IPv4 sockets
		_ = syscall.SetsockoptInt(int(fd), syscall.IPPROTO_IPV6, ipv6TrafficClass, tos)
	})
	if err != nil {
		return err
	}
	return opErr
}
