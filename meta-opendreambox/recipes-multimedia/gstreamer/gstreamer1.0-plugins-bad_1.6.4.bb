include recipes-multimedia/gstreamer/gstreamer1.0-plugins-bad.inc

LIC_FILES_CHKSUM = "file://COPYING;md5=73a5855a8119deb017f5f13cf327095d \
                    file://gst/tta/filters.h;beginline=12;endline=29;md5=8a08270656f2f8ad7bb3655b83138e5a \
                    file://COPYING.LIB;md5=21682e4e8fea52413fd26c60acb907e5 \
                    file://gst/tta/crc32.h;beginline=12;endline=29;md5=27db269c575d1e5317fffca2d33b3b50"

SRC_URI = "${@get_gst_srcuri(d)}"
SRC_URI += " \
    file://use-automake-1.12.patch \
    file://dvdspu-overlay-composition.patch \
    file://dvdspu-forced-mode-property.patch \
    file://dvdspu-dts-timestamps.patch \
    file://assrender-overlay-caps.patch \
    file://rtmpsrc-interruption.patch \
"

SRC_URI[md5sum] = "6768524cb8bcdcaf1345d9c66f3bd7bd"
SRC_URI[sha256sum] = "2b98df8d4d7784d5186baf2e19f565a6d8f8924119f8470e23c96889aaa603fe"

S = "${WORKDIR}/gst-plugins-bad-${PV}"

EXTRA_OECONF += " \
    --enable-vcd \
    --disable-bs2b \
    --disable-libvisual \
    --disable-openexr \
    --disable-libde265 \
    --disable-openh264 \
    --disable-openni2 \
    --disable-winks \
    --disable-x265 \
"
