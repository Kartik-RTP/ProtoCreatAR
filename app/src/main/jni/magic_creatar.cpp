#include <AR/gsub_es.h>
#include <Eden/glm.h>
#include <jni.h>
#include <ARWrapper/ARToolKitWrapperExportedAPI.h>
#include <unistd.h> // chdir()
#include <android/log.h>

// Utility preprocessor directive so only one change needed if Java class name changes
#define JNIFUNCTION_DEMO(sig) Java_com_kartikgupta_myapplication_helper_SperoRenderer_##sig //need to come up with proper name for this literal

extern "C" {
	JNIEXPORT void JNICALL JNIFUNCTION_DEMO(Initialise(JNIEnv* env, jobject object));
	JNIEXPORT void JNICALL JNIFUNCTION_DEMO(Shutdown(JNIEnv* env, jobject object));
	JNIEXPORT void JNICALL JNIFUNCTION_DEMO(SurfaceCreated(JNIEnv* env, jobject object));
	JNIEXPORT void JNICALL JNIFUNCTION_DEMO(SurfaceChanged(JNIEnv* env, jobject object, jint w, jint h));
	JNIEXPORT bool JNICALL JNIFUNCTION_DEMO(DrawFrame(JNIEnv* env, jobject obj));	
	JNIEXPORT int JNICALL JNIFUNCTION_DEMO(AddMarkerAndModel(JNIEnv* env, jobject object, jstring modelfileString , jstring marker_config_string));
	JNIEXPORT void JNICALL JNIFUNCTION_DEMO(DeleteMarkerAndModel(JNIEnv* env, jobject object , jint marker_index));
	
};

typedef struct ARModel {
	int patternID;
	ARdouble transformationMatrix[16];
	bool visible;
	GLMmodel* obj;
	bool not_null;
} ARModel;

#define NUM_MODELS 10 //this will be the maximum number of markers that creatAR will allow to keep on app at any point of time
static ARModel models[NUM_MODELS] ;
//TODO : checkout if the above declaration can be made dynamic somehow
static float lightAmbient[4] = {0.1f, 0.1f, 0.1f, 1.0f};
static float lightDiffuse[4] = {1.0f, 1.0f, 1.0f, 1.0f};
static float lightPosition[4] = {0.0f, 0.0f, 1.0f, 0.0f};

JNIEXPORT void JNICALL JNIFUNCTION_DEMO(Initialise(JNIEnv* env, jobject object)) {
	


	for(int i=0;i<NUM_MODELS;i++){
		
			models[i].patternID=-1;
	//		models[i].transformationMatrix=PUT_SOME_PLACEHOLDER_TEMPORARY_VARIABLE_HERE;
			models[i].visible=false;
			models[i].obj=NULL;
			models[i].not_null=false;
		 //mean they are null ...
	}


	/* SOME SAMPLE CODE FOR REFERENCE


	const char *model0file = "Data/models/Porsche_911_GT3.obj";
	const char *model1file = "Data/models/Ferrari_Modena_Spider.obj";

	models[0].patternID = arwAddMarker("single;Data/hiro.patt;80");
	arwSetMarkerOptionBool(models[0].patternID, ARW_MARKER_OPTION_SQUARE_USE_CONT_POSE_ESTIMATION, false);
	arwSetMarkerOptionBool(models[0].patternID, ARW_MARKER_OPTION_FILTERED, true);

	models[0].obj = glmReadOBJ2(model0file, 0, 0); // context 0, don't read textures yet.
	if (!models[0].obj) {
		LOGE("Error loading model from file '%s'.", model0file);
		exit(-1);
	}
	glmScale(models[0].obj, 0.035f);
	//glmRotate(models[0].obj, 3.14159f / 2.0f, 1.0f, 0.0f, 0.0f);
	glmCreateArrays(models[0].obj, GLM_SMOOTH | GLM_MATERIAL | GLM_TEXTURE);
	models[0].visible = false;
	
	models[1].patternID = arwAddMarker("single;Data/kanji.patt;80");
	arwSetMarkerOptionBool(models[1].patternID, ARW_MARKER_OPTION_SQUARE_USE_CONT_POSE_ESTIMATION, false);
	arwSetMarkerOptionBool(models[1].patternID, ARW_MARKER_OPTION_FILTERED, true);

	models[1].obj = glmReadOBJ2(model1file, 0, 0); // context 0, don't read textures yet.
	if (!models[1].obj) {
		LOGE("Error loading model from file '%s'.", model1file);
		exit(-1);
	}
	glmScale(models[1].obj, 0.035f);
	//glmRotate(models[1].obj, 3.14159f / 2.0f, 1.0f, 0.0f, 0.0f);
	glmCreateArrays(models[1].obj, GLM_SMOOTH | GLM_MATERIAL | GLM_TEXTURE);
	models[1].visible = false;
*/

}

JNIEXPORT void JNICALL JNIFUNCTION_DEMO(Shutdown(JNIEnv* env, jobject object)) {
}

JNIEXPORT int JNICALL JNIFUNCTION_DEMO(AddMarkerAndModel(JNIEnv* env, jobject object, jstring modelfileString , jstring marker_config_string)) {
	
	int free_marker_space_index=-1;

	for (int i = 0; i < NUM_MODELS; i++) {
	    if (models[i].not_null==false) {
	        free_marker_space_index = i;
	        break;
	    }
	}
	if(free_marker_space_index==-1){
		//means no space for marker left
		return free_marker_space_index;
	}
	const char *marker_config = env->GetStringUTFChars( marker_config_string, 0);
	models[free_marker_space_index].patternID = arwAddMarker(marker_config);
	env->ReleaseStringUTFChars(marker_config_string, marker_config);


	arwSetMarkerOptionBool(models[free_marker_space_index].patternID, ARW_MARKER_OPTION_SQUARE_USE_CONT_POSE_ESTIMATION, false);
	arwSetMarkerOptionBool(models[free_marker_space_index].patternID, ARW_MARKER_OPTION_FILTERED, true);
	
	const char *modelfile = env->GetStringUTFChars( modelfileString, 0);
	models[free_marker_space_index].obj = glmReadOBJ2(modelfile, 0, 0); // context 0, don't read textures yet.
	if (!models[free_marker_space_index].obj) {
		LOGE("Error loading model from file '%s'.", modelfile);
		exit(-1);
	}
	env->ReleaseStringUTFChars(modelfileString, modelfile);
	
	LOGV("just checking this function..no error..don't worry");
	//printf("just checking this function..no error..don't worry");
	glmScale(models[free_marker_space_index].obj, 0.035f);
	//glmRotate(models[0].obj, 3.14159f / 2.0f, 1.0f, 0.0f, 0.0f);
	glmCreateArrays(models[free_marker_space_index].obj, GLM_SMOOTH | GLM_MATERIAL | GLM_TEXTURE);
	models[free_marker_space_index].visible = false;
	models[free_marker_space_index].not_null=true;

	
	return free_marker_space_index;
}


JNIEXPORT void JNICALL JNIFUNCTION_DEMO(DeleteMarkerAndModel(JNIEnv* env, jobject object , jint marker_index)) {
	models[marker_index].not_null=false; 
	arwRemoveMarker(models[marker_index].patternID);
	//this marker space will no longer be used

}
JNIEXPORT void JNICALL JNIFUNCTION_DEMO(SurfaceCreated(JNIEnv* env, jobject object)) {
	glStateCacheFlush(); // Make sure we don't hold outdated OpenGL state.
	for (int i = 0; i < NUM_MODELS; i++) {
	    if (models[i].obj) {
	        glmDelete(models[i].obj, 0);
	        models[i].obj = NULL;
	    }
	}
}

JNIEXPORT void JNICALL JNIFUNCTION_DEMO(SurfaceChanged(JNIEnv* env, jobject object, jint w, jint h)) {
	// glViewport(0, 0, w, h) has already been set.
}

JNIEXPORT bool JNICALL JNIFUNCTION_DEMO(DrawFrame(JNIEnv* env, jobject obj)) {
	
	glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
	glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); 

    // Set the projection matrix to that provided by ARToolKit.
	float proj[16];
	arwGetProjectionMatrix(proj);
	glMatrixMode(GL_PROJECTION);
	glLoadMatrixf(proj);
	glMatrixMode(GL_MODELVIEW);
	
	glStateCacheEnableDepthTest();
	glStateCacheEnableLighting();	
	glEnable(GL_LIGHT0);
	
	for (int i = 0; i < NUM_MODELS; i++) {		
		if(models[i].not_null){
			
			models[i].visible = arwQueryMarkerTransformation(models[i].patternID, models[i].transformationMatrix);		
			
			if (models[i].visible) {					
				glLoadMatrixf(models[i].transformationMatrix);		
				glLightfv(GL_LIGHT0, GL_AMBIENT, lightAmbient);
				glLightfv(GL_LIGHT0, GL_DIFFUSE, lightDiffuse);
				glLightfv(GL_LIGHT0, GL_POSITION, lightPosition);
				glmDrawArrays(models[i].obj, 0);
				return true;
			}	
		}

	}
	return false;
	
}
