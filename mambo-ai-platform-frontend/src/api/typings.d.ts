declare namespace API {
  type adminDeleteUserParams = {
    id: number
  }

  type AppAddRequest = {
    initPrompt?: string
    codeGenType?: string
    isVipOnly?: boolean
  }

  type AppAdminUpdateRequest = {
    id?: number
    appName?: string
    cover?: string
    priority?: number
  }

  type AppDeployRequest = {
    appId?: number
  }

  type AppQueryRequest = {
    pageNum?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    id?: number
    appName?: string
    cover?: string
    initPrompt?: string
    codeGenType?: string
    deployKey?: string
    priority?: number
    userId?: number
    isVipOnly?: boolean
  }

  type AppUpdateRequest = {
    id?: number
    appName?: string
    priority?: number
    isVipOnly?: boolean
  }

  type AppVO = {
    id?: number
    appName?: string
    cover?: string
    initPrompt?: string
    codeGenType?: string
    deployKey?: string
    deployedTime?: string
    priority?: number
    userId?: number
    isVipOnly?: boolean
    createTime?: string
    updateTime?: string
    user?: UserPublicVO
  }

  type BaseResponseAppVO = {
    code?: number
    data?: AppVO
    message?: string
  }

  type BaseResponseBoolean = {
    code?: number
    data?: boolean
    message?: string
  }

  type BaseResponseListUserAdminVO = {
    code?: number
    data?: UserAdminVO[]
    message?: string
  }

  type BaseResponseLoginUserVO = {
    code?: number
    data?: LoginUserVO
    message?: string
  }

  type BaseResponseLong = {
    code?: number
    data?: number
    message?: string
  }

  type BaseResponseObject = {
    code?: number
    data?: Record<string, any>
    message?: string
  }

  type BaseResponsePageAppVO = {
    code?: number
    data?: PageAppVO
    message?: string
  }

  type BaseResponsePageChatHistory = {
    code?: number
    data?: PageChatHistory
    message?: string
  }

  type BaseResponsePageUserAdminVO = {
    code?: number
    data?: PageUserAdminVO
    message?: string
  }

  type BaseResponsePageUserPublicVO = {
    code?: number
    data?: PageUserPublicVO
    message?: string
  }

  type BaseResponseString = {
    code?: number
    data?: string
    message?: string
  }

  type BaseResponseUser = {
    code?: number
    data?: User
    message?: string
  }

  type BaseResponseUserDetailVO = {
    code?: number
    data?: UserDetailVO
    message?: string
  }

  type BaseResponseUserPublicVO = {
    code?: number
    data?: UserPublicVO
    message?: string
  }

  type cancelGenParams = {
    appId: number
  }

  type ChatHistory = {
    id?: number
    message?: string
    messageType?: string
    appId?: number
    userId?: number
    createTime?: string
    updateTime?: string
    isDelete?: number
  }

  type ChatHistoryQueryRequest = {
    pageNum?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    id?: number
    message?: string
    messageType?: string
    appId?: number
    userId?: number
    lastCreateTime?: string
  }

  type chatToGenCodeParams = {
    appId: number
    message: string
  }

  type DeleteRequest = {
    id?: number
  }

  type DiagramTask = {
    mermaidCode?: string
    description?: string
  }

  type downloadAppCodeParams = {
    appId: number
  }

  type executeWorkflowParams = {
    prompt: string
  }

  type executeWorkflowWithFluxParams = {
    prompt: string
  }

  type executeWorkflowWithSseParams = {
    prompt: string
  }

  type getAppVOByIdByAdminParams = {
    id: number
  }

  type getAppVOByIdParams = {
    id: number
  }

  type getGenStatusParams = {
    appId: number
  }

  type getInfoParams = {
    id: number
  }

  type getUserByIdParams = {
    id: number
  }

  type getUserVOByIdParams = {
    id: number
  }

  type IllustrationTask = {
    query?: string
  }

  type ImageCollectionPlan = {
    contentImageTasks?: ImageSearchTask[]
    illustrationTasks?: IllustrationTask[]
    diagramTasks?: DiagramTask[]
    logoTasks?: LogoTask[]
  }

  type ImageResource = {
    category?: 'CONTENT' | 'LOGO' | 'ILLUSTRATION' | 'ARCHITECTURE'
    description?: string
    url?: string
  }

  type ImageSearchTask = {
    query?: string
  }

  type listAppChatHistoryParams = {
    appId: number
    pageSize?: number
    lastCreateTime?: string
  }

  type LoginUserVO = {
    id?: number
    userAccount?: string
    userName?: string
    userAvatar?: string
    userRole?: string
    createTime?: string
    updateTime?: string
    isVip?: boolean
    vipExpireTime?: string
    shareCode?: string
    loginTime?: string
  }

  type LogoTask = {
    description?: string
  }

  type PageAppVO = {
    records?: AppVO[]
    pageNumber?: number
    pageSize?: number
    totalPage?: number
    totalRow?: number
    optimizeCountQuery?: boolean
  }

  type PageChatHistory = {
    records?: ChatHistory[]
    pageNumber?: number
    pageSize?: number
    totalPage?: number
    totalRow?: number
    optimizeCountQuery?: boolean
  }

  type pageParams = {
    page: PageUser
  }

  type PageUser = {
    records?: User[]
    pageNumber?: number
    pageSize?: number
    totalPage?: number
    totalRow?: number
    optimizeCountQuery?: boolean
  }

  type PageUserAdminVO = {
    records?: UserAdminVO[]
    pageNumber?: number
    pageSize?: number
    totalPage?: number
    totalRow?: number
    optimizeCountQuery?: boolean
  }

  type PageUserPublicVO = {
    records?: UserPublicVO[]
    pageNumber?: number
    pageSize?: number
    totalPage?: number
    totalRow?: number
    optimizeCountQuery?: boolean
  }

  type QualityResult = {
    isValid?: boolean
    errors?: string[]
    suggestions?: string[]
  }

  type ServerSentEventString = true

  type serveStaticResourceParams = {
    deployKey: string
  }

  type SseEmitter = {
    timeout?: number
  }

  type User = {
    id?: number
    userAccount?: string
    userPassword?: string
    userName?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
    editTime?: string
    createTime?: string
    updateTime?: string
    isDelete?: number
    vipExpireTime?: string
    vipNumber?: number
    shareCode?: string
    inviteUser?: number
    isVip?: boolean
  }

  type UserAddRequest = {
    userName?: string
    userAccount?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
  }

  type UserAdminVO = {
    id?: number
    userAccount?: string
    userName?: string
    userAvatar?: string
    userRole?: string
    createTime?: string
    updateTime?: string
    editTime?: string
    vipExpireTime?: string
    vipNumber?: number
    inviteUser?: number
    isVip?: boolean
  }

  type UserDetailVO = {
    id?: number
    userAccount?: string
    userName?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
    createTime?: string
    updateTime?: string
    vipExpireTime?: string
    vipNumber?: number
    shareCode?: string
    hasInviter?: boolean
    isVip?: boolean
  }

  type UserLoginRequest = {
    userAccount?: string
    userPassword?: string
  }

  type UserPublicVO = {
    id?: number
    userName?: string
    userAvatar?: string
    userProfile?: string
    isVip?: boolean
    createTime?: string
  }

  type UserQueryRequest = {
    pageNum?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    id?: number
    userAccount?: string
    userName?: string
    userProfile?: string
    userRole?: string
    isVip?: boolean
  }

  type UserRegisterRequest = {
    userAccount?: string
    userPassword?: string
    checkPassword?: string
  }

  type UserUpdateRequest = {
    id?: number
    userName?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
    vipExpireTime?: string
    vipNumber?: number
    inviteUser?: number
  }

  type WorkflowContext = {
    currentStep?: string
    originalPrompt?: string
    imageListStr?: string
    imageList?: ImageResource[]
    enhancedPrompt?: string
    generationType?: 'HTML' | 'MULTI_FILE' | 'VUE_PROJECT'
    generatedCodeDir?: string
    buildResultDir?: string
    qualityResult?: QualityResult
    errorMessage?: string
    imageCollectionPlan?: ImageCollectionPlan
    contentImages?: ImageResource[]
    illustrations?: ImageResource[]
    diagrams?: ImageResource[]
    logos?: ImageResource[]
  }
}
