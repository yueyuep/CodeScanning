# -*- mode: python ; coding: utf-8 -*-

block_cipher = None


a = Analysis(['SimResult.py','Utils.py','ParseFile.py','Graph.py'],
             pathex=['H:\\tools'],
             binaries=[],
             datas=[],
             hiddenimports=['sklearn','grakel.kernels._isomorphism.__init__','scipy','grakel.kernels.graphlet_sampling.','grakel.kernels.__init__',
             'grakel.graph_kernels','grakel.__init__'],
             hookspath=[],
             runtime_hooks=[],
             excludes=[],
             win_no_prefer_redirects=False,
             win_private_assemblies=False,
             cipher=block_cipher,
             noarchive=False)
pyz = PYZ(a.pure, a.zipped_data,
             cipher=block_cipher)
exe = EXE(pyz,
          a.scripts,
          [],
          exclude_binaries=True,
          name='SimResult',
          debug=False,
          bootloader_ignore_signals=False,
          strip=False,
          upx=True,
          console=True )
coll = COLLECT(exe,
               a.binaries,
               a.zipfiles,
               a.datas,
               strip=False,
               upx=True,
               upx_exclude=[],
               name='SimResult')
