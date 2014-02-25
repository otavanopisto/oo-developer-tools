package fi.muikku.dnm.downloader

def wget(address) {
  def result = new URL(address).openStream().getText()
}

def download(folderNo, name, arg3, arg4, arg5) {
  def extraOpts="${arg3} ${arg4} ${arg5}"

  
  def args="--folder-no $folderNo --name $name --nexus-user $NEXUS_USER " +
       "--nexus-password $NEXUS_PASSWORD --zip-password $ZIP_PASSWORD " +
       "--output-dir $OUTPUT_DIR --import-url $DOWNLOAD_URL --binary-lookup " +
       "$BINARY_LOOKUP --cache-folder $CACHE_FOLDER $extraOpts"
  
  Downloader.invoke(args.split(/ /))
}

def download_import(folderNo, workspaceUrl, file, arg3="", arg4="", arg5="") {
  def completeFile="$OUTPUT_DIR$file"
  
  println "\n~~~~~~~~~~~~~~~~~~~~~~~~~"
  println "Downloading $file"
  download folderNo, file, arg3, arg4, arg5
  println "Importing $file"
  wget "$IMPORT_URL?targetFolder=$workspaceUrl&file=$completeFile"
  println "~~~~~~~~~~~~~~~~~~~~~~~~~\n"
}

def download_all() {
  new File(BINARY_LOOKUP).delete()
  
  for (Map material : MATERIALS) {
    download_import material.folderNo, material.workspaceUrl, material.file, material.get('extra', '')
  }
}

evaluate(new File("config.groovy"))

download_all()