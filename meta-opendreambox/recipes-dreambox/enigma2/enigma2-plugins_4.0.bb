SUMMARY = "Additional plugins for Enigma2"
LICENSE = "Proprietary"
LIC_FILES_CHKSUM = "file://COPYING;md5=8e37f34d0e40d32ea2bc90ee812c9131"
DEPENDS = " \
        dbttcd \
        dvdbackup \
        enigma2 \
        hdparm \
        python-daap \
        python-flickrapi \
        python-gdata \
        python-mutagen \
        python-pycrypto \
        python-pyopenssl \
        python-transmissionrpc \
        smartmontools \
        streamripper \
"
SRCREV = "${@opendreambox_srcrev('5882a1da86fd2eea3169a02a469dff4442965909', d)}"

SRC_URI += "file://print.mak"

GITHUB_BRANCH = "4.0"

inherit autotools opendreambox-github pythonnative

export BUILD_SYS
export HOST_SYS
export STAGING_INCDIR
export STAGING_LIBDIR

do_install_append() {
        # create lists of files installed outside of "${libdir}/enigma2/python/Plugins" or "${datadir}/meta"
        rm -rf ${INSTALL_ROOTDIR}
        install -d ${INSTALL_ROOTDIR}
        SUBDIRS=`make -f Makefile -f ${WORKDIR}/print.mak print-SUBDIRS`
        for SUBDIR in $SUBDIRS; do
                oe_runmake -C $SUBDIR "DESTDIR=${INSTALL_DESTDIR}" install
                CATEGORY=`ls -1 ${INSTALL_PLUGINSDIR} | head -n1 | tr '[:upper:]' '[:lower:]'`
                NAME=`echo $SUBDIR | tr '[:upper:]' '[:lower:]'`
                PKG="enigma2-plugin-$CATEGORY-$NAME"
                rm -rf ${INSTALL_METADIR} ${INSTALL_PLUGINSDIR}
                find ${INSTALL_DESTDIR} -type f -name "*.la" -delete -fprintf ${INSTALL_ROOTDIR}/$PKG-dev '/%P\n'
                [ -s ${INSTALL_ROOTDIR}/$PKG-dev ] || rm ${INSTALL_ROOTDIR}/$PKG-dev
                find ${INSTALL_DESTDIR} -type f -name "*.a" -delete -fprintf ${INSTALL_ROOTDIR}/$PKG-staticdev '/%P\n'
                [ -s ${INSTALL_ROOTDIR}/$PKG-staticdev ] || rm ${INSTALL_ROOTDIR}/$PKG-staticdev
                find ${INSTALL_DESTDIR} -type f -fprintf ${INSTALL_ROOTDIR}/$PKG '/%P\n'
                [ -s ${INSTALL_ROOTDIR}/$PKG ] || rm ${INSTALL_ROOTDIR}/$PKG
                rm -rf ${INSTALL_DESTDIR}
        done
}

PACKAGES_DYNAMIC = "enigma2-plugin-*"
PACKAGES += "${PN}-meta"

FILES_${PN} += "${datadir}/enigma2 ${datadir}/fonts"
FILES_${PN}-meta = "${datadir}/meta"

python populate_packages_prepend() {
    enigma2_plugindir = bb.data.expand('${libdir}/enigma2/python/Plugins', d)
    do_split_packages(d, enigma2_plugindir, '^(\w+/\w+)', 'enigma2-plugin-%s', '%s', recursive=True, match_path=True, prepend=True)
    do_split_packages(d, enigma2_plugindir, '^(\w+/\w+).*/.*\.la$', 'enigma2-plugin-%s-dev', '%s (development)', recursive=True, match_path=True, prepend=True)
    do_split_packages(d, enigma2_plugindir, '^(\w+/\w+).*/.*\.a$', 'enigma2-plugin-%s-staticdev', '%s (static development)', recursive=True, match_path=True, prepend=True)
    do_split_packages(d, enigma2_plugindir, '^(\w+/\w+).*/\.debug/', 'enigma2-plugin-%s-dbg', '%s (debug)', recursive=True, match_path=True, prepend=True)
    def parseControlFile(dir, d, package):
        src = open(dir + "/" + package.split('-')[-1] + "/CONTROL/control").read()
        for line in src.splitlines():
            try:
                name, value = line.strip().split(': ', 1)
                if name == 'Description':
                    d.setVar('DESCRIPTION_' + package, value)
                elif name == 'Depends':
                    d.setVar('RDEPENDS_' + package, ' '.join(value.split(', ')))
                elif name == 'Replaces':
                    d.setVar('RREPLACES_' + package, ' '.join(value.split(', ')))
                elif name == 'Conflicts':
                    d.setVar('RCONFLICTS_' + package, ' '.join(value.split(', ')))
            except:
                bb.fatal("Error parsing control file for package %s" % package)
    def parseFileList(dir, d, package):
        filename = os.path.join(dir, package)
        if os.path.exists(filename):
            varname = 'FILES_%s' % package
            files = (d.getVar(varname, True) or "").split()
            src = open(filename).read()
            for line in src.splitlines():
                if not line in files:
                    files.append(line)
            d.setVar(varname, ' '.join(files))
    tempdir = d.getVar('INSTALL_ROOTDIR', True)
    srcdir = d.getVar('S', True)
    for package in d.getVar('PACKAGES', True).split():
        if package.startswith('enigma2-plugin-'):
            parseFileList(tempdir, d, package)
            if not package.endswith('-dev') and not package.endswith('-dbg') and not package.endswith('-staticdev'):
                parseControlFile(srcdir, d, package)
}

INSTALL_ROOTDIR = "${WORKDIR}/${PN}-packaging-tempdir"
INSTALL_DESTDIR = "${INSTALL_ROOTDIR}/destdir"
INSTALL_METADIR = "${INSTALL_DESTDIR}${datadir}/meta"
INSTALL_PLUGINSDIR = "${INSTALL_DESTDIR}${libdir}/enigma2/python/Plugins"

COMPATIBLE_MACHINE = "^(dm500hd|dm500hdv2|dm800se|dm800sev2|dm7020hd|dm7020hdv2|dm8000)$"
