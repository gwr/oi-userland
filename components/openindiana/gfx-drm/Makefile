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
#

include ../../../make-rules/shared-macros.mk

COMPONENT_NAME=		gfx-drm
COMPONENT_SRC=		$(COMPONENT_NAME)

GIT=git
GIT_REPO=https://github.com/alarcher/gfx-drm
GIT_BRANCH=drm3
GIT_CHANGESET=HEAD

RELEASE_MINOR=3
ONNV_BUILDNUM=$(BRANCHID)
COMPONENT_REVISION=$(shell cd $(COMPONENT_SRC); git rev-list HEAD --count)

GFX_DRM_REPO=$(SOURCE_DIR)/packages/$(MACH)/nightly-nd/repo.gfx-drm

# The code is not lint clean: do not pass -l
# Do not check proto area as we do not install drm headers -N
NIGHTLY_OPTIONS=-nmprtN

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
PATCHES= $(shell find $(PATCH_DIR) $(PARFAIT_PATCH_DIR) -type f \
											-name '$(PATCH_PATTERN)' \
                      	2>/dev/null | sort) $(EXTRA_PATCHES)

$(SOURCE_DIR)/.patched:	$(SOURCE_DIR)/.downloaded $(PATCHES)
	$(MKDIR) $(@D)
	cd $(SOURCE_DIR) && \
        $(GIT) checkout -f && \
              $(GIT) clean -f
	for p in $(PATCHES); do \
          $(GPATCH) -d $(@D) $(GPATCH_FLAGS) < $$p; \
        done
	@cd $(SOURCE_DIR); $(GIT) log -1 --format=%H > .downloaded
	$(TOUCH) $@

prep::	$(SOURCE_DIR)/.patched

$(BUILD_DIR)/$(MACH)/.built: $(SOURCE_DIR)/.patched
	cd $(SOURCE_DIR) && \
		cat myenv.sh | \
		(sed \
			-e 's|^export NIGHTLY_OPTIONS=.*|export NIGHTLY_OPTIONS=\"$(NIGHTLY_OPTIONS)\"|' \
			-e 's|^export VERSION=.*|export VERSION=\"$$(git log -1 --format=illumos-%h)\"|' \
			-e 's|^export CODEMGR_WS=.*|export CODEMGR_WS=\"$$PWD\"|' \
			-e 's|^export ON_CLOSED_BINS=.*|export ON_CLOSED_BINS=\"/opt/onbld/closed\"|'; \
		echo export PYTHON_VERSION=\"$(PYTHON_VERSION)\"; \
		echo export ONNV_BUILDNUM=$(ONNV_BUILDNUM); \
		echo export PKGVERS_BRANCH=$(ONNV_BUILDNUM); \
		) > env.sh && \
	time $(ENV) -i /opt/onbld/bin/nightly env.sh
	@[ -d $(BUILD_DIR)/$(MACH) ] || $(MKDIR) $(BUILD_DIR)/$(MACH)
	$(TOUCH) $@

PROTO_DIR=$(SOURCE_DIR)/proto/root_i386/

build install: $(BUILD_DIR)/$(MACH)/.built

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
