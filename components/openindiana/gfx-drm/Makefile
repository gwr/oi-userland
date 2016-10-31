#
# This file and its contents are supplied under the terms of the
# Common Development and Distribution License ("CDDL)". You may
# only use this file in accordance with the terms of the CDDL.
#
# A full copy of the text of the CDDL should have accompanied this
# source. A copy of the CDDL is also available via the Internet at
# http://www.illumos.org/license/CDDL.
#

#
# Copyright 2016 Alexander Pyhalov
# Copyright 2016 Aurelien Larcher
# Copyright 2016 Gordon Ross
#

include ../../../make-rules/shared-macros.mk

COMPONENT_NAME=		gfx-drm
COMPONENT_SRC=		$(COMPONENT_NAME)

GIT=git
#GIT_REPO=git://github.com/alarcher/gfx-drm.git
GIT_REPO=git://github.com/gwr/gfx-drm.git
GIT_BRANCH=drm6lib
GIT_CHANGESET=HEAD

FETCH=$(WS_TOOLS)/userland-fetch

RELEASE_MINOR=3
ONNV_BUILDNUM=$(BRANCHID)

COMPONENT_REVISION=$(shell cd $(COMPONENT_SRC); git rev-list HEAD --count)

GFX_DRM_REPO=$(SOURCE_DIR)/packages/$(MACH)/nightly-nd/repo.skel

CLEAN_PATHS += $(BUILD_DIR)
CLOBBER_PATHS += $(SOURCE_DIR)

$(SOURCE_DIR)/.downloaded: 
	@[ -d $(SOURCE_DIR) ] || \
	$(GIT) clone -b $(GIT_BRANCH) $(GIT_REPO) $(SOURCE_DIR)
	@cd $(SOURCE_DIR); $(GIT) checkout $(GIT_BRANCH); $(GIT) pull --rebase \
	  $(GIT_REPO) $(GIT_BRANCH); $(GIT) log -1 --format=%H > .downloaded

update:
	@[ -d $(SOURCE_DIR) ] || \
	$(GIT) clone -b $(GIT_BRANCH) $(GIT_REPO) $(SOURCE_DIR)
	cd $(SOURCE_DIR); $(GIT) pull --rebase $(GIT_REPO) $(GIT_BRANCH); \
	  [ "$$($(GIT) log -1 --format=%H)" == "$$(cat .downloaded)" ] || \
	  $(GIT) log -1 --format=%H > .downloaded

download:: $(SOURCE_DIR)/.downloaded

PATCH_DIR?= patches
PATCH_PATTERN?= *.patch
PATCHES= # please don't patch -- update gfx-drm instead

$(SOURCE_DIR)/.patched:	$(SOURCE_DIR)/.downloaded $(PATCHES)
	$(MKDIR) $(@D)
	cd $(SOURCE_DIR) && \
        $(GIT) checkout -f && \
              $(GIT) clean -f
	$(TOUCH) $@

prep::	$(SOURCE_DIR)/.patched

$(BUILD_DIR)/$(MACH)/.built: $(SOURCE_DIR)/.patched env.sh
	cd $(SOURCE_DIR) && \
		/opt/onbld/bin/bldenv ../env.sh "cd usr/src; make install"
	cd $(SOURCE_DIR) && \
		/opt/onbld/bin/bldenv ../env.sh "cd usr/src/pkg; make install"
	@[ -d $(BUILD_DIR)/$(MACH) ] || $(MKDIR) $(BUILD_DIR)/$(MACH)
	$(TOUCH) $@

PROTO_DIR=$(SOURCE_DIR)/proto/root_i386/

build install: $(BUILD_DIR)/$(MACH)/.built

# Could we just provide a transforms directory here instead?
# BTW, what ../illumos-gate does here is gross.
$(BUILD_DIR)/$(MACH)/publish.transforms:
	echo "<transform set name=pkg.fmri -> edit value pkg://[^/]+/ pkg://$(PUBLISHER)/>" > \
	$(BUILD_DIR)/$(MACH)/publish.transforms
	echo "<transform set name=pkg.fmri -> edit value ,.+: ,$(BUILD_VERSION):>" >> \
	$(BUILD_DIR)/$(MACH)/publish.transforms
	echo "<transform set name=pkg.fmri -> emit set name=userland.info.git-remote value=$(USERLAND_GIT_REMOTE)>" >> \
	$(BUILD_DIR)/$(MACH)/publish.transforms
	echo "<transform set name=pkg.fmri -> emit set name=userland.info.git-branch value=$(USERLAND_GIT_BRANCH)>" >> \
	$(BUILD_DIR)/$(MACH)/publish.transforms
	echo "<transform set name=pkg.fmri -> emit set name=userland.info.git-rev value=$(USERLAND_GIT_REV)>" >> \
	$(BUILD_DIR)/$(MACH)/publish.transforms
	echo "<transform set name=pkg.fmri -> emit set name=illumos-gate.info.git-remote value=$(GIT_REPO)>" >> \
	$(BUILD_DIR)/$(MACH)/publish.transforms
	echo "<transform set name=pkg.fmri -> emit set name=illumos-gate.info.git-branch value=$(GIT_BRANCH)>" >> \
	$(BUILD_DIR)/$(MACH)/publish.transforms
	echo "<transform set name=pkg.fmri -> emit set name=illumos-gate.info.git-rev value=$(shell cd $(COMPONENT_SRC); git rev-parse HEAD)>" >> \
	$(BUILD_DIR)/$(MACH)/publish.transforms

$(BUILD_DIR)/$(MACH)/.published:  $(BUILD_DIR)/$(MACH)/publish.transforms 
	$(RM) -r $(@D)/pkgrecv.dir
	$(MKDIR) $(@D)/pkgrecv.dir
	pkgrepo -s $(GFX_DRM_REPO) rebuild
	pkgrecv -s $(GFX_DRM_REPO) -d $(@D)/pkgrecv.dir --raw \
	  $$(pkgrecv -s $(GFX_DRM_REPO) --newest | sed -f packages.ignore)
	for pkg in $$(echo $(@D)/pkgrecv.dir/*/*); do \
		pkgmogrify -O $$pkg/manifest $$pkg/manifest \
		$(BUILD_DIR)/$(MACH)/publish.transforms; \
		pkgsend -s $(WS_REPO) publish --fmri-in-manifest \
			-d $(BUILD_DIR)/$(MACH)/overlay -d $$pkg $$pkg/manifest; \
	done
	$(TOUCH) $@

.NOTPARALLEL:

publish: update $(BUILD_DIR)/$(MACH)/.published

clean::
	$(RM) -r $(CLEAN_PATHS)

clobber::       clean
	$(RM) -r $(CLOBBER_PATHS)

REQUIRED_PACKAGES += developer/build/onbld
